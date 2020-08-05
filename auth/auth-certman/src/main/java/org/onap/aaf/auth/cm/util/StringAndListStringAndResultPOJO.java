package org.onap.aaf.auth.cm.util;

import org.onap.aaf.auth.cm.data.CertResp;
import org.onap.aaf.auth.layer.Result;

import java.util.List;

/**
 * A POJO for use in CMService, containing only a String object, a List<String>
 * object, and a Result object. This can be safely removed if CMService is ever
 * refactored to throw custom exceptions which are then translated into
 * Results, but for the time being this seemed the most expedient and
 * elegant solution.
 *
 * @author sh265m
 */

public class StringAndListStringAndResultPOJO {
    private String string;
    private List<String> stringList;
    private Result<CertResp> result;

    public StringAndListStringAndResultPOJO() {
        // empty constructor
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public Result<CertResp> getResult() {
        return result;
    }

    public void setResult(Result<CertResp> result) {
        this.result = result;
    }
}
