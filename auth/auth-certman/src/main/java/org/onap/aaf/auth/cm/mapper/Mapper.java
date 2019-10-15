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

package org.onap.aaf.auth.cm.mapper;

import java.io.IOException;
import java.util.List;

import org.onap.aaf.auth.cm.data.CertDrop;
import org.onap.aaf.auth.cm.data.CertRenew;
import org.onap.aaf.auth.cm.data.CertReq;
import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.dao.cass.ArtiDAO;
import org.onap.aaf.auth.dao.cass.CertDAO;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;

public interface Mapper<REQ,CERT,ARTIFACTS,ERROR>
{
    public enum API{ERROR,VOID,CERT,CERT_REQ,CERT_RENEW,CERT_DROP,ARTIFACTS};

    public Class<?> getClass(API api);
    public<A> A newInstance(API api);

    public ERROR errorFromMessage(StringBuilder holder, String msgID, String text, Object ... detail);

    public Result<CERT> toCert(AuthzTrans trans, Result<CertResp> in, boolean withTrustChain) throws IOException;
    public Result<CERT> toCert(AuthzTrans trans, Result<List<CertDAO.Data>> in);

    public Result<CertReq> toReq(AuthzTrans trans, REQ req);
    public Result<CertRenew> toRenew(AuthzTrans trans, REQ req);
    public Result<CertDrop>  toDrop(AuthzTrans trans, REQ req);

    public List<ArtiDAO.Data> toArtifact(AuthzTrans trans, ARTIFACTS arti);
    public Result<ARTIFACTS> fromArtifacts(Result<List<ArtiDAO.Data>> readArtifactsByMachine);
}
