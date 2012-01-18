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

package org.dcm4che.net.service;

import java.io.IOException;
import java.util.Arrays;

import org.dcm4che.data.Attributes;
import org.dcm4che.data.Tag;
import org.dcm4che.data.VR;
import org.dcm4che.net.Association;
import org.dcm4che.net.AssociationStateException;
import org.dcm4che.net.Commands;
import org.dcm4che.net.Status;
import org.dcm4che.net.pdu.PresentationContext;

/**
 * @author Gunter Zeilinger <gunterze@gmail.com>
 *
 */
public class BasicNActionSCP extends DicomService implements NActionSCP {

    private int[] actionTypeIDs;

    public BasicNActionSCP(String... sopClasses) {
        super(sopClasses);
    }

    public void setActionTypeIDs(int... actionTypeIDs) {
        int[] a = actionTypeIDs.clone();
        Arrays.sort(a);
        this.actionTypeIDs = a;
    }

    private void checkActionTypeID(int actionTypeID) throws DicomServiceException {
        if (actionTypeIDs != null
                && Arrays.binarySearch(actionTypeIDs, actionTypeID) < 0)
            throw new DicomServiceException(Status.NoSuchActionType)
                        .setActionTypeID(actionTypeID);
    }

    @Override
    public void onNActionRQ(Association as, PresentationContext pc,
            Attributes rq, Attributes actionInfo) throws IOException {
        int actionTypeID = rq.getInt(Tag.ActionTypeID, 0);
        checkActionTypeID(actionTypeID);
        Attributes rsp = Commands.mkRSP(rq, Status.Success);
        Object[] handback = new Object[1];
        Attributes actionReply = action(as, pc, actionTypeID, actionInfo, rsp,
                handback);
        if (actionReply != null)
            rsp.setInt(Tag.ActionTypeID, VR.US, actionTypeID);
        try {
            as.writeDimseRSP(pc, rsp, actionReply);
            postNActionRSP(as, pc, actionTypeID, actionInfo, rsp, handback[0]);
        } catch (AssociationStateException e) {
            LOG.warn("{} << N-ACTION-RSP failed: {}", as, e.getMessage());
        }
    }

    protected Attributes action(Association as, PresentationContext pc,
            int actionTypeID, Attributes actionInfo, Attributes rsp,
            Object[] handback) throws DicomServiceException {
        return null;
    }

    protected void postNActionRSP(Association as, PresentationContext pc,
            int actionTypeID, Attributes actionInfo, Attributes rsp,
            Object handback) {
    }
}