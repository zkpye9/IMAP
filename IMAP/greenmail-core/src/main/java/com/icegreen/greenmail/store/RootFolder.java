/* -------------------------------------------------------------------
* This software is released under the Apache license 2.0
* -------------------------------------------------------------------
*/
package com.icegreen.greenmail.store;

import com.icegreen.greenmail.imap.ImapConstants;

/**
 * @author Raimund Klein <raimund.klein@gmx.de>
 */
class RootFolder extends HierarchicalFolder {
    public RootFolder() {
        super(null, ImapConstants.USER_NAMESPACE);
    }

    @Override
    public String getFullName() {
        return name;
    }
}
