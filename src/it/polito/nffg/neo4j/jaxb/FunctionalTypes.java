//
// Questo file è stato generato dall'architettura JavaTM per XML Binding (JAXB) Reference Implementation, v2.2.8-b130911.1802 
// Vedere <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Qualsiasi modifica a questo file andrà persa durante la ricompilazione dello schema di origine. 
// Generato il: 2017.03.01 alle 04:27:21 PM CET 
//


package it.polito.nffg.neo4j.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java per functionalTypes.
 * 
 * <p>Il seguente frammento di schema specifica il contenuto previsto contenuto in questa classe.
 * <p>
 * <pre>
 * &lt;simpleType name="functionalTypes">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ACL_FIREWALL"/>
 *     &lt;enumeration value="END_HOST"/>
 *     &lt;enumeration value="ANTISPAM"/>
 *     &lt;enumeration value="CACHE"/>
 *     &lt;enumeration value="DPI"/>
 *     &lt;enumeration value="MAILCLIENT"/>
 *     &lt;enumeration value="MAILSERVER"/>
 *     &lt;enumeration value="NAT"/>
 *     &lt;enumeration value="VPN_ACCESS"/>
 *     &lt;enumeration value="VPN_EXIT"/>
 *     &lt;enumeration value="WEBCLIENT"/>
 *     &lt;enumeration value="WEBSERVER"/>
 *     &lt;enumeration value="FIELDMODIFIER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "functionalTypes")
@XmlEnum
public enum FunctionalTypes {

    ACL_FIREWALL,
    END_HOST,
    ANTISPAM,
    CACHE,
    DPI,
    MAILCLIENT,
    MAILSERVER,
    NAT,
    VPN_ACCESS,
    VPN_EXIT,
    WEBCLIENT,
    WEBSERVER,
    FIELDMODIFIER;

    public String value() {
        return name();
    }

    public static FunctionalTypes fromValue(String v) {
        return valueOf(v);
    }

}
