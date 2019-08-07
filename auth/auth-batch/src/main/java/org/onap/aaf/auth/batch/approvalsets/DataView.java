/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */
package org.onap.aaf.auth.batch.approvalsets;

import java.util.List;

import org.onap.aaf.auth.dao.cass.ApprovalDAO;
import org.onap.aaf.auth.dao.cass.FutureDAO;
import org.onap.aaf.auth.dao.cass.NsDAO;
import org.onap.aaf.auth.dao.cass.RoleDAO;
import org.onap.aaf.auth.dao.cass.UserRoleDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

/**
 * I have become convinced that Data for Apps is modeled by abstract access methods against multiple data
 * sources.  With the insistence of JUnits, it becomes much more paramount to create a model which can 
 *   1) be easily loaded from Disk "Test Data" without resorting to complex "mokito" schemes
 *   2) tested in Memory
 *   3) combined for REAL time by running Cached Memory
 *   4) Streamable in
 *       a) Binary
 *      b) CSV
 *      c) JSON
 *      d) XML
 *   5) persisted Globally through a store like Cassandra
 *   
 * But in the end, it looks like:
 *   1) Data Structures
 *   2) Find the Data Structures by various means, accounting for 
 *       a) Multiple Responses
 *      b) Errors from the deepest level, made available through the call stack
 *   3) 
 *     
 * @author jonathan.gathman
 *
 */
public interface DataView {
    // Reads
    public Result<NsDAO.Data> ns(final AuthzTrans trans, final String id);
    public Result<RoleDAO.Data> roleByName(final AuthzTrans trans, final String name);
    public Result<List<UserRoleDAO.Data>> ursByRole(final AuthzTrans trans, final String role);
    public Result<List<UserRoleDAO.Data>> ursByUser(final AuthzTrans trans, final String user);

    // Inserts
    public Result<ApprovalDAO.Data> insert(final AuthzTrans trans, final ApprovalDAO.Data add);
    public Result<FutureDAO.Data> insert(final AuthzTrans trans, final FutureDAO.Data add);
    
    // Deletes
    public Result<ApprovalDAO.Data> delete(final AuthzTrans trans, final ApprovalDAO.Data add);
    public Result<FutureDAO.Data> delete(final AuthzTrans trans, final FutureDAO.Data add);
    
    // Clear any buffers
    public void flush();
}
