/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.controller.server;

import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.*;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.bean.TaskItem;
import com.evolveum.midpoint.web.controller.util.ControllerUtil;
import com.evolveum.midpoint.web.controller.util.SortableListController;
import com.evolveum.midpoint.web.repo.RepositoryManager;
import com.evolveum.midpoint.web.util.FacesUtils;
import com.evolveum.midpoint.xml.ns._public.common.common_1.NodeType;
import com.evolveum.midpoint.xml.ns._public.common.common_1.TaskType;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller("taskList")
@Scope("session")
public class TaskListController extends SortableListController<TaskItem> {

    private static final transient Trace LOGGER = TraceManager.getTrace(TaskListController.class);

    private static final long serialVersionUID = 1L;
    @Autowired(required = true)
    private transient TaskManager taskManager;
    @Autowired(required = true)
    private transient TaskDetailsController taskEdit;
    private TaskItem selectedTask;
    @Autowired(required = true)
    private transient RepositoryManager repositoryManager;

    private ClusterStatusInformation clusterStatusInformation;

    private boolean listAll = false;
    private boolean selectAll = false;

    // private Set<TaskItem> runningTasks;
    private boolean activated;

    public static final String PAGE_NAVIGATION = "/admin/server/index?faces-redirect=true";
    public static final String PAGE_LEFT_NAVIGATION = "leftRunnableTasks";
    private static final String PARAM_TASK_OID = "taskOid";

    public TaskListController() {
        super();
    }

    @Override
    protected String listObjects() {
        List<PrismObject<TaskType>> taskTypeList = repositoryManager.listObjects(TaskType.class, getOffset(),
                getRowsCount());
        clusterStatusInformation = taskManager.getRunningTasksClusterwide();

        List<TaskItem> runningTasks = getObjects();
        runningTasks.clear();
        for (PrismObject<TaskType> task : taskTypeList) {
            runningTasks.add(new TaskItem(task, taskManager, clusterStatusInformation));
        }

        listAll = true;
        return PAGE_NAVIGATION;
    }

    public String listRunningTasks() {

        Set<Task> tasks = null;
        try {
            tasks = taskManager.getRunningTasks();
        } catch (TaskManagerException e) {
            LoggingUtils.logException(LOGGER, "Cannot get the list of tasks", e);
            tasks = new HashSet<Task>();
        }
        List<TaskItem> runningTasks = getObjects();
        runningTasks.clear();
        for (Task task : tasks) {
            runningTasks.add(new TaskItem(task, taskManager, null));
        }

        clusterStatusInformation = null;            // to ensure that cluster info will not display old information

        listAll = false;
        return PAGE_NAVIGATION;
    }

    public String listRunningTasksClusterwide() {

        OperationResult result = createOperationResult("listRunningTasksClusterwide");

        clusterStatusInformation = taskManager.getRunningTasksClusterwide();
        List<TaskItem> runningTasks = getObjects();
        runningTasks.clear();
        for (ClusterStatusInformation.TaskInfo taskInfo : clusterStatusInformation.getTasks()) {
            String oid = taskInfo.getOid();
            try {
                Task task = taskManager.getTask(oid, result);
                runningTasks.add(new TaskItem(task, taskManager, clusterStatusInformation));
            } catch (ObjectNotFoundException e) {
                LoggingUtils.logException(LOGGER, "Cannot retrieve the task {} from repository, because it does not exist.", e, oid);
            } catch (SchemaException e) {
                LoggingUtils.logException(LOGGER, "Cannot retrieve the task {} from repository due to schema exception.", e, oid);
            } catch (SystemException e) {
                LoggingUtils.logException(LOGGER, "Cannot retrieve the task {} from repository due to system exception.", e, oid);
            }
        }

        listAll = false;
        return PAGE_NAVIGATION;
    }

    private TaskItem getSelectedTaskItem() {
        String taskOid = FacesUtils.getRequestParameter(PARAM_TASK_OID);
        if (StringUtils.isEmpty(taskOid)) {
            FacesUtils.addErrorMessage("Task oid not defined in request.");
            return null;
        }

        TaskItem taskItem = getTaskItem(taskOid);
        if (StringUtils.isEmpty(taskOid)) {
            FacesUtils.addErrorMessage("Task for oid '" + taskOid + "' not found.");
            return null;
        }

        return taskItem;
    }

    private TaskItem getTaskItem(String resourceOid) {
        for (TaskItem item : getObjects()) {
            if (item.getOid().equals(resourceOid)) {
                return item;
            }
        }

        return null;
    }

    public String deleteTask() {

        if (selectedTask.getOid() == null) {
            FacesUtils.addErrorMessage("No task to delete defined");
            throw new IllegalArgumentException("No task to delete defined.");
        }

        OperationResult result = new OperationResult(TaskDetailsController.class.getName() + ".deleteTask");
        result.addParam("taskOid", selectedTask.getOid());

        try {
        	String oid = selectedTask.getOid();
        	
        	boolean getInfoProblem = false;
        	boolean wasRunning = false;
        	boolean isRunning = false;
        	
        	try {
        		Task task = taskManager.getTask(oid, result);
        		wasRunning = taskManager.isTaskThreadActive(oid);
        		if (wasRunning)
        			isRunning = !taskManager.suspendTask(task, 1000L, result);
        	}
        	catch (Exception ex) {		// we don't care which kind of exception occurred while getting information about the task
            	getInfoProblem = true;
            	result.recordPartialError("It was not possible to get the information about the task. Reason: " + ex.getMessage(), ex);
        	}
        	
        	taskManager.deleteTask(oid, result);
            getObjects().remove(selectedTask);
            
            if (getInfoProblem)
            	FacesUtils.addWarnMessage("The task was successfully removed from the repository. " +
            			"However, it was not possible to get its state; it might " +
            			"be still running. Please check using the 'List currently executing tasks' function.");
            else if (!wasRunning)		// was not running
            	FacesUtils.addSuccessMessage("Task has been successfully deleted.");
            else if (!isRunning)		// was running, but now it is stopped
            	FacesUtils.addSuccessMessage("Task has been successfully shut down and deleted.");
            else
            	FacesUtils.addWarnMessage("The task was successfully removed from the repository. " +
            			"However, although requested to shut down, it has not stopped yet. " + 
            			"Please check using the 'List currently executing tasks' function.");

        } catch (Exception ex) {
            FacesUtils.addErrorMessage(
                    "Task with oid " + selectedTask.getOid() + " not found. Reason: " + ex.getMessage(), ex);
            result.recordFatalError(
                    "Task with oid " + selectedTask.getOid() + " not found. Reason: " + ex.getMessage(), ex);
            return null;
        }

        return PAGE_NAVIGATION;
    }

    public String showTaskDetails() {
        selectedTask = getSelectedTaskItem();
        if (selectedTask == null) {
            return null;
        }

        taskEdit.setTask(selectedTask);
//		List<OperationResultType> opResultList = new ArrayList<OperationResultType>();
//		if (selectedTask.getResult() != null) {
//
//			OperationResultType opResultType = selectedTask.getResult().createOperationResultType();
//			opResultList.add(opResultType);
//			long token = 1220000000000000000L;
//			getResult(opResultType, opResultList, token);
//			taskDetails.setResults(opResultList);
//		} else {
//			taskDetails.setResults(opResultList);
//		}
        // template.setSelectedLeftId(ResourceDetailsController.NAVIGATION_LEFT);
        return TaskDetailsController.PAGE_NAVIGATION;
    }

//	public void getResult(OperationResultType opResult, List<OperationResultType> opResultList, long token) {
//
//		for (OperationResultType result : opResult.getPartialResults()) {
//			result.setToken(result.getToken() + token);
//			opResultList.add(result);
//			token += 1110000000000000000L;
//			getResult(result, opResultList, token);
//		}
//
//	}

    public void sortList(ActionEvent evt) {
        sort();
    }

    @Override
    protected void sort() {
        // Collections.sort(getObjects(), new
        // SortableListComparator<TaskItem>(getSortColumnName(),isAscending()));

    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    public void selectAllPerformed(ValueChangeEvent event) {
        ControllerUtil.selectAllPerformed(event, getObjects());
    }

    public void selectPerformed(ValueChangeEvent evt) {
        this.selectAll = ControllerUtil.selectPerformed(evt, getObjects());
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public String deactivate() {
        if (taskManager.deactivateServiceThreads(1000L)) {
            FacesUtils.addSuccessMessage("All task manager threads have been deactivated. Tasks are left in RUNNABLE state, but they will not be executed on this node until service threads are reactivated.");
        } else {
            FacesUtils.addWarnMessage("Task manager threads were requested to stop. Either some of them are still alive, or there was some problem. Please see the log.");
        }
        return null;
    }

    public void reactivate() {
        OperationResult result = new OperationResult("reactivate service threads.");
        taskManager.reactivateServiceThreads(result)        ;
        FacesUtils.addMessage(result);
//        if () {
//            FacesUtils.addSuccessMessage("All task manager threads have been reactivated.");
//        } else {
//            FacesUtils.addErrorMessage("Task manager threads could not be reactivated, please see the log.");
//        }
    }

//    public boolean isActivated() {
//        return taskManager.getServiceThreadsActivationState();
//    }

//    public void setActivated(boolean activated) {
//        this.activated = activated;
//    }

    public TaskItem getSelectedTask() {
        return selectedTask;
    }

    public void setSelectedTask(TaskItem selectedTask) {
        this.selectedTask = selectedTask;
    }

    public boolean isListAll() {
        return listAll;
    }

    public void setListAll(boolean listAll) {
        this.listAll = listAll;
    }

    // for debugging
    public String getClusterInfo() {

        if (clusterStatusInformation == null) {
            return "No cluster info; local scheduler is " + (taskManager.getServiceThreadsActivationState() ? "" : "NOT") + " running";
        }

        StringBuffer retval = new StringBuffer();
        retval.append("Cluster nodes: \n");         // newlines are not displayed
        for (Node node : clusterStatusInformation.getNodes()) {
            NodeType nodeType = node.getNodeType().asObjectable();
            retval.append("{Node " + nodeType.getNodeIdentifier() + " (" + nodeType.getHostname() + "): ");
            if (node.isConnectionError()) {
                retval.append("*** Connection error: " + node.getConnectionError() + " ***");
            } else {
                retval.append("running = " + node.isSchedulerRunning());

                List<ClusterStatusInformation.TaskInfo> taskInfoList = clusterStatusInformation.getTasksOnNode(node);
                retval.append("; tasks: " + taskInfoList.size() + " [");
                boolean first = true;
                for (ClusterStatusInformation.TaskInfo ti : taskInfoList) {
                    if (first) {
                        first = false;
                    } else {
                        retval.append(", ");
                    }
                    retval.append(ti.getOid());
                }
                retval.append("]");
            }
            retval.append("}   \n");
        }
        return retval.toString();
    }

    private OperationResult createOperationResult(String methodName) {
        return new OperationResult(TaskListController.class.getName() + "." + methodName);
    }

}
