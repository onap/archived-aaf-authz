package org.onap.aaf.auth.cm.util;

import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.layer.Result;

import java.net.InetAddress;

/**
 * A POJO for use in CMService, containing only an InetAddress object
 * and a Result object. This can be safely removed if CMService is ever
 * refactored to throw custom exceptions which are then translated into
 * Results, but for the time being this seemed the most expedient and
 * elegant solution.
 *
 * @author sh265m
 */

public class InetAddressAndResultPOJO {
    private InetAddress inetAddress;
    private Result<CertResp> result;

    public InetAddressAndResultPOJO() {
        // empty constructor
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public Result<CertResp> getResult() {
        return result;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }

    public void setResult(Result<CertResp> result) {
        this.result = result;
    }
}