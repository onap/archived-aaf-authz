package org.onap.aaf.auth.cm.util;

import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.dao.cass.ArtiDAO;
import org.onap.aaf.auth.layer.Result;

/**
 * A POJO for use in CMService, containing only an ArtiDao.Data object
 * and a Result object. This can be safely removed if CMService is ever
 * refactored to throw custom exceptions which are then translated into
 * Results, but for the time being this seemed the most expedient and
 * elegant solution.
 *
 * @author sh265m
 */

public class ArtiDaoDataAndResultPOJO {
    private ArtiDAO.Data data;
    private Result<CertResp> result;

    public ArtiDaoDataAndResultPOJO() {
        // empty constructor
    }

    public ArtiDAO.Data getData() {
        return data;
    }

    public Result<CertResp> getResult() {
        return result;
    }

    public void setData(ArtiDAO.Data data) {
        this.data = data;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}
