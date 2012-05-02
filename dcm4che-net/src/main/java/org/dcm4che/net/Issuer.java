/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2012
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che.net;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.util.StringUtils;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 */
public class Issuer {

    private final String localNamespaceEntityID;
    private final String universalEntityID;
    private final String universalEntityIDType;

    public Issuer(String localNamespaceEntityID, String universalEntityID,
            String universalEntityIDType) {
        this.localNamespaceEntityID = localNamespaceEntityID;
        this.universalEntityID = universalEntityID;
        this.universalEntityIDType = universalEntityIDType;
        validate();
    }

    public Issuer(String s) {
        this(s, '^');
    }

    public Issuer(String s, char delim) {
        String[] ss = StringUtils.split(s, delim);
        if (ss.length > 3)
            throw new IllegalArgumentException(s);
        this.localNamespaceEntityID = emptyToNull(ss[0]);
        this.universalEntityID = ss.length > 1 ? emptyToNull(ss[1]) : null;
        this.universalEntityIDType = ss.length > 2 ? emptyToNull(ss[2]) : null;
        validate();
    }

    private void validate() {
        if (localNamespaceEntityID == null && universalEntityID == null)
            throw new IllegalArgumentException(
                    "Missing Local Namespace Entity ID or Universal Entity ID");
        if (universalEntityID != null) {
            if (universalEntityIDType == null)
                throw new IllegalArgumentException("Missing Universal Entity ID Type");
        } else {
            if (universalEntityIDType != null)
                throw new IllegalArgumentException("Missing Universal Entity ID");
        }
    }

    private String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
    }

    public final String getLocalNamespaceEntityID() {
        return localNamespaceEntityID;
    }

    public final String getUniversalEntityID() {
        return universalEntityID;
    }

    public final String getUniversalEntityIDType() {
        return universalEntityIDType;
    }

    @Override
    public int hashCode() {
        return 37 * (
                37 * hashCode(localNamespaceEntityID)
                   + hashCode(universalEntityID))
                + hashCode(universalEntityIDType);
    }

    private int hashCode(String s) {
        return s == null ? 0 : s.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Issuer))
            return false;
        Issuer other = (Issuer) o;
        return equals(localNamespaceEntityID, other.localNamespaceEntityID)
                && equals(universalEntityID, other.universalEntityID)
                && equals(universalEntityIDType, other.universalEntityIDType);
    }

    private boolean equals(String s1, String s2) {
        return s1 == s2 || s1 != null && s1.equals(s2);
    }

    public boolean matches(Issuer other) {
        if (this == other)
            return true;

        int equalsID, equalsUID, equalsUIDType;
        if ((equalsID = matches(localNamespaceEntityID, other.localNamespaceEntityID)) < 0
                || (equalsUID = matches(universalEntityID, other.universalEntityID)) < 0
                || (equalsUIDType = matches(universalEntityIDType, other.universalEntityIDType)) < 0)
            return false;
        return equalsID > 0 || equalsUID > 0 && equalsUIDType > 0;
    }

    private int matches(String s1, String s2) {
        return s1 == null || s2 == null ? 0 : s1.equals(s2) ? 1 : -1;
    }

    @Override
    public String toString() {
        return toString('^');
    }

    public String toString(char delim) {
        return universalEntityID == null
            ? localNamespaceEntityID
            : (localNamespaceEntityID == null ? "" : localNamespaceEntityID) + delim
                        + universalEntityID  + delim
                        + universalEntityIDType;
    }

    public Attributes toItem() {
        int size = 0;
        if (localNamespaceEntityID != null)
            size++;
        if (universalEntityID != null)
            size++;
        if (universalEntityIDType != null)
            size++;

        Attributes item = new Attributes(size);
        if (localNamespaceEntityID != null)
            item.setString(Tag.LocalNamespaceEntityID, VR.UT, localNamespaceEntityID);
        if (universalEntityID != null)
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
        if (universalEntityIDType != null)
            item.setString(Tag.UniversalEntityIDType, VR.UT, universalEntityIDType);
        return item ;
    }

    public Attributes toIssuerOfPatientID(Attributes attrs) {
        if (attrs == null)
            attrs = new Attributes(2);
        if (localNamespaceEntityID != null)
            attrs.setString(Tag.IssuerOfPatientID, VR.LO, localNamespaceEntityID);
        if (universalEntityID != null) {
            Attributes item = new Attributes(2);
            item.setString(Tag.UniversalEntityID, VR.UT, universalEntityID);
            item.setString(Tag.UniversalEntityIDType, VR.UT, universalEntityIDType);
            attrs.newSequence(Tag.IssuerOfPatientIDQualifiersSequence, 1).add(item);
        }
        return attrs;
    }

    public static Issuer valueOf(Attributes item) {
        if (item == null || item.isEmpty())
            return null;

        return new Issuer(
                item.getString(Tag.LocalNamespaceEntityID),
                item.getString(Tag.UniversalEntityID),
                item.getString(Tag.UniversalEntityIDType));
    }

    public static Issuer issuerOfPatientIDOf(Attributes attrs) {
        String entityID = attrs.getString(Tag.IssuerOfPatientID);
        Attributes item = attrs.getNestedDataset(Tag.IssuerOfPatientIDQualifiersSequence);
        if (entityID == null && (item == null || item.isEmpty()))
            return null;

        return item != null
                ? new Issuer(
                        entityID,
                        item.getString(Tag.UniversalEntityID),
                        item.getString(Tag.UniversalEntityIDType))
                : new Issuer(entityID, null, null);
    }

}