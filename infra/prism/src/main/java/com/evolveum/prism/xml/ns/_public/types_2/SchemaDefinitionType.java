//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.06 at 06:55:18 PM CET 
//


package com.evolveum.prism.xml.ns._public.types_2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import com.evolveum.midpoint.util.DOMUtil;


/**
 * 
 *                 Type for prism schema definition. It currently contains
 *                 XSD schema definition. But other languages may be supported
 *                 later.
 *             
 * 
 * <p>Java class for SchemaDefinitionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SchemaDefinitionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;any namespace='##other' maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SchemaDefinitionType", propOrder = {
    "any"
})
public class SchemaDefinitionType implements Serializable {

	public static final QName COMPLEX_TYPE = new QName("http://prism.evolveum.com/xml/ns/public/types-2", "SchemaDefinitionType");
	
    @XmlAnyElement
    protected List<Element> any;

    @XmlTransient
    protected Element schema;
    
    public Element getSchema() {
		return schema;
	}

	public void setSchema(Element schema) {
		this.schema = schema;
	}

	/**
     * Gets the value of the any property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the any property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAny().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Element }
     * 
     * 
     */
    public List<Element> getAny() {
        if (any == null) {
            any = new List<Element>() {

				@Override
				public int size() {
					if (schema == null) {
						return 0;
					} else {
						return 1;
					}
				}

				@Override
				public boolean isEmpty() {
					return schema == null;
				}

				@Override
				public boolean contains(Object o) {
					return o.equals(schema);
				}

				@Override
				public Iterator<Element> iterator() {
					// Very lazy implementation: TODO: cleanup if needed
					return new Iterator<Element>() {
						private int index = 0;
						
						@Override
						public boolean hasNext() {
							return index == 0;
						}

						@Override
						public Element next() {
							index++;
							return schema;
						}

						@Override
						public void remove() {
							schema = null;
						}
					};
				}

				@Override
				public Object[] toArray() {
					if (schema == null) {
						return new Object[0];
					} else {
						Object[] a = new Object[1];
						a[0] = schema;
						return a;
					}
				}

				@Override
				public <T> T[] toArray(T[] a) {
					return (T[]) toArray();
				}

				@Override
				public boolean add(Element e) {
					if (schema == null) {
						schema = e;
						return true;
					} else {
						throw new IllegalStateException("Cannot add more then one schema element");
					}
				}

				@Override
				public boolean remove(Object o) {
					if (o.equals(schema)) {
						schema = null;
						return true;
					} else {
						return false;
					}
				}

				@Override
				public boolean containsAll(Collection<?> c) {
					for (Object e: c) {
						if (!contains(e)) {
							return false;
						}
					}
					return true;
				}

				@Override
				public boolean addAll(Collection<? extends Element> c) {
					boolean changed = false;
					for (Element e: c) {
						if (add(e)) {
							changed = true;
						}
					}
					return changed;
				}

				@Override
				public boolean addAll(int index, Collection<? extends Element> c) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public boolean removeAll(Collection<?> c) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public boolean retainAll(Collection<?> c) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public void clear() {
					schema = null;
				}

				@Override
				public Element get(int index) {
					if (index == 0) {
						return schema;
					} else {
						return null;
					}
				}

				@Override
				public Element set(int index, Element element) {
					if (index == 0) {
						schema = element;
						return schema;
					} else {
						throw new IndexOutOfBoundsException();
					}
				}

				@Override
				public void add(int index, Element element) {
					if (index == 0 && schema == null) {
						schema = element;
					} else {
						throw new IndexOutOfBoundsException();
					}
				}

				@Override
				public Element remove(int index) {
					if (index == 0) {
						Element old = schema;
						schema = null;
						return old;
					} else {
						throw new IndexOutOfBoundsException();
					}

				}

				@Override
				public int indexOf(Object o) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public int lastIndexOf(Object o) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public ListIterator<Element> listIterator() {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public ListIterator<Element> listIterator(int index) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}

				@Override
				public List<Element> subList(int fromIndex, int toIndex) {
					throw new UnsupportedOperationException("Lazyness is one of the greatest virtues of a programmer");
				}
			};
        }
        return this.any;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((schema == null) ? 0 : schema.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		return equals(obj, false);
	}

	public boolean equals(Object obj, boolean isLiteral) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SchemaDefinitionType other = (SchemaDefinitionType) obj;
		if (schema == null) {
			if (other.schema != null)
				return false;
		} else if (!DOMUtil.compareElement(schema, other.schema, isLiteral))
			return false;
		return true;
	}
}
