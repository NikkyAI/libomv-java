//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.03.20 at 10:56:33 PM CET 
//

package libomv.ImportExport.collada;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for anonymous complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bind_shape_matrix" type="{http://www.collada.org/2005/11/COLLADASchema}float4x4" minOccurs="0"/>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}source" maxOccurs="unbounded" minOccurs="3"/>
 *         &lt;element name="joints">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded" minOccurs="2"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="vertex_weights">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocalOffset" maxOccurs="unbounded" minOccurs="2"/>
 *                   &lt;element name="vcount" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfUInts" minOccurs="0"/>
 *                   &lt;element name="v" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfInts" minOccurs="0"/>
 *                   &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attribute name="count" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}uint" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="sourceUri" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "bindShapeMatrix", "source", "joints", "vertexWeights", "extra" })
@XmlRootElement(name = "skin")
public class Skin {

	@XmlList
	@XmlElement(name = "bind_shape_matrix", type = Double.class)
	protected List<Double> bindShapeMatrix;
	@XmlElement(required = true)
	protected List<Source> source;
	@XmlElement(required = true)
	protected Skin.Joints joints;
	@XmlElement(name = "vertex_weights", required = true)
	protected Skin.VertexWeights vertexWeights;
	protected List<Extra> extra;
	@XmlAttribute(name = "sourceUri", required = true)
	@XmlSchemaType(name = "anyURI")
	protected String sourceUri;

	/**
	 * Gets the value of the bindShapeMatrix property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot.
	 * Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
	 * for the bindShapeMatrix property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getBindShapeMatrix().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Double }
	 * 
	 * 
	 */
	public List<Double> getBindShapeMatrix() {
		if (bindShapeMatrix == null) {
			bindShapeMatrix = new ArrayList<Double>();
		}
		return this.bindShapeMatrix;
	}

	/**
	 * 
	 * The skin element must contain at least three source elements. Gets the value
	 * of the source property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot.
	 * Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
	 * for the source property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getSource().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Source }
	 * 
	 * 
	 */
	public List<Source> getSource() {
		if (source == null) {
			source = new ArrayList<Source>();
		}
		return this.source;
	}

	/**
	 * Gets the value of the joints property.
	 * 
	 * @return possible object is {@link Skin.Joints }
	 * 
	 */
	public Skin.Joints getJoints() {
		return joints;
	}

	/**
	 * Sets the value of the joints property.
	 * 
	 * @param value
	 *            allowed object is {@link Skin.Joints }
	 * 
	 */
	public void setJoints(Skin.Joints value) {
		this.joints = value;
	}

	/**
	 * Gets the value of the vertexWeights property.
	 * 
	 * @return possible object is {@link Skin.VertexWeights }
	 * 
	 */
	public Skin.VertexWeights getVertexWeights() {
		return vertexWeights;
	}

	/**
	 * Sets the value of the vertexWeights property.
	 * 
	 * @param value
	 *            allowed object is {@link Skin.VertexWeights }
	 * 
	 */
	public void setVertexWeights(Skin.VertexWeights value) {
		this.vertexWeights = value;
	}

	/**
	 * 
	 * The extra element may appear any number of times. Gets the value of the extra
	 * property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a snapshot.
	 * Therefore any modification you make to the returned list will be present
	 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
	 * for the extra property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getExtra().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list {@link Extra }
	 * 
	 * 
	 */
	public List<Extra> getExtra() {
		if (extra == null) {
			extra = new ArrayList<Extra>();
		}
		return this.extra;
	}

	/**
	 * Gets the value of the sourceUri property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getSourceUri() {
		return sourceUri;
	}

	/**
	 * Sets the value of the sourceUri property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setSourceUri(String value) {
		this.sourceUri = value;
	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained within
	 * this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocal" maxOccurs="unbounded" minOccurs="2"/>
	 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
	 *       &lt;/sequence>
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "input", "extra" })
	public static class Joints {

		@XmlElement(required = true)
		protected List<InputLocal> input;
		protected List<Extra> extra;

		/**
		 * Gets the value of the input property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot.
		 * Therefore any modification you make to the returned list will be present
		 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
		 * for the input property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getInput().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link InputLocal }
		 * 
		 * 
		 */
		public List<InputLocal> getInput() {
			if (input == null) {
				input = new ArrayList<InputLocal>();
			}
			return this.input;
		}

		/**
		 * 
		 * The extra element may appear any number of times. Gets the value of the extra
		 * property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot.
		 * Therefore any modification you make to the returned list will be present
		 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
		 * for the extra property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getExtra().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link Extra }
		 * 
		 * 
		 */
		public List<Extra> getExtra() {
			if (extra == null) {
				extra = new ArrayList<Extra>();
			}
			return this.extra;
		}

	}

	/**
	 * <p>
	 * Java class for anonymous complex type.
	 * 
	 * <p>
	 * The following schema fragment specifies the expected content contained within
	 * this class.
	 * 
	 * <pre>
	 * &lt;complexType>
	 *   &lt;complexContent>
	 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
	 *       &lt;sequence>
	 *         &lt;element name="input" type="{http://www.collada.org/2005/11/COLLADASchema}InputLocalOffset" maxOccurs="unbounded" minOccurs="2"/>
	 *         &lt;element name="vcount" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfUInts" minOccurs="0"/>
	 *         &lt;element name="v" type="{http://www.collada.org/2005/11/COLLADASchema}ListOfInts" minOccurs="0"/>
	 *         &lt;element ref="{http://www.collada.org/2005/11/COLLADASchema}extra" maxOccurs="unbounded" minOccurs="0"/>
	 *       &lt;/sequence>
	 *       &lt;attribute name="count" use="required" type="{http://www.collada.org/2005/11/COLLADASchema}uint" />
	 *     &lt;/restriction>
	 *   &lt;/complexContent>
	 * &lt;/complexType>
	 * </pre>
	 * 
	 * 
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "", propOrder = { "input", "vcount", "v", "extra" })
	public static class VertexWeights {

		@XmlElement(required = true)
		protected List<InputLocalOffset> input;
		@XmlList
		protected List<BigInteger> vcount;
		@XmlList
		@XmlElement(type = Long.class)
		protected List<Long> v;
		protected List<Extra> extra;
		@XmlAttribute(name = "count", required = true)
		protected BigInteger count;

		/**
		 * Gets the value of the input property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot.
		 * Therefore any modification you make to the returned list will be present
		 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
		 * for the input property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getInput().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list
		 * {@link InputLocalOffset }
		 * 
		 * 
		 */
		public List<InputLocalOffset> getInput() {
			if (input == null) {
				input = new ArrayList<InputLocalOffset>();
			}
			return this.input;
		}

		/**
		 * Gets the value of the vcount property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot.
		 * Therefore any modification you make to the returned list will be present
		 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
		 * for the vcount property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getVcount().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link BigInteger }
		 * 
		 * 
		 */
		public List<BigInteger> getVcount() {
			if (vcount == null) {
				vcount = new ArrayList<BigInteger>();
			}
			return this.vcount;
		}

		/**
		 * Gets the value of the v property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot.
		 * Therefore any modification you make to the returned list will be present
		 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
		 * for the v property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getV().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link Long }
		 * 
		 * 
		 */
		public List<Long> getV() {
			if (v == null) {
				v = new ArrayList<Long>();
			}
			return this.v;
		}

		/**
		 * 
		 * The extra element may appear any number of times. Gets the value of the extra
		 * property.
		 * 
		 * <p>
		 * This accessor method returns a reference to the live list, not a snapshot.
		 * Therefore any modification you make to the returned list will be present
		 * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
		 * for the extra property.
		 * 
		 * <p>
		 * For example, to add a new item, do as follows:
		 * 
		 * <pre>
		 * getExtra().add(newItem);
		 * </pre>
		 * 
		 * 
		 * <p>
		 * Objects of the following type(s) are allowed in the list {@link Extra }
		 * 
		 * 
		 */
		public List<Extra> getExtra() {
			if (extra == null) {
				extra = new ArrayList<Extra>();
			}
			return this.extra;
		}

		/**
		 * Gets the value of the count property.
		 * 
		 * @return possible object is {@link BigInteger }
		 * 
		 */
		public BigInteger getCount() {
			return count;
		}

		/**
		 * Sets the value of the count property.
		 * 
		 * @param value
		 *            allowed object is {@link BigInteger }
		 * 
		 */
		public void setCount(BigInteger value) {
			this.count = value;
		}

	}

}
