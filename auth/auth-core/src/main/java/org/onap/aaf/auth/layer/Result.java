/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2018 AT&T Intellectual Property. All rights reserved.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *      http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.aaf.auth.layer;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * It would be nice if Java Enums were extensible, but they're not.
 * <p>
 * @author Jonathan
 *
 */
public class Result<RV> {
    private static final String SUCCESS = "Success";
    public static final Object[] EMPTY_VARS = new Object[0];

    public final static int OK=0,
                            ERR_Security                 = 1,
                            ERR_Denied                     = 2,
                            ERR_Policy                     = 3,
                            ERR_BadData                 = 4,
                            ERR_NotImplemented             = 5,
                            ERR_NotFound                 = 6,
                            ERR_ConflictAlreadyExists     = 7,
                            ERR_ActionNotCompleted         = 8,
                            ERR_Backend                    = 9,
                            ERR_General                    = 20;
                        
    public RV value;
    public final int status;
    public final String details;
    public final Object[] variables;

    public Result(RV value, int status, String details, Object ... variables) {
        this.value = value;
        if (value==null) {
        specialCondition|=EMPTY_LIST;
        }
        this.status = status;
        this.details = details;
        if (variables==null) {
            this.variables = EMPTY_VARS;
        } else {
            this.variables=variables;
        }
    }

    /**
     * Create a Result class with "OK" status and "Success" for details
     * <p>
     * This is the easiest to use
     * <p>
     * @param value
     * @param status
     * @return
     */
    public static<R> Result<R> ok(R value) {
        return new Result<R>(value,OK,SUCCESS,EMPTY_VARS);
    }

    /**
     * Accept Arrays and mark as empty or not
     * @param value
     * @return
     */
    public static<R> Result<R[]> ok(R value[]) {
        return new Result<R[]>(value,OK,SUCCESS,EMPTY_VARS).emptyList(value.length==0);
    }

    /**
     * Accept Sets and mark as empty or not
     * @param value
     * @return
     */
    public static<R> Result<Set<R>> ok(Set<R> value) {
        return new Result<Set<R>>(value,OK,SUCCESS,EMPTY_VARS).emptyList(value.size()==0);
    }

    /**
     * Accept Lists and mark as empty or not
     * @param value
     * @return
     */
    public static<R> Result<List<R>> ok(List<R> value) {
        return new Result<List<R>>(value,OK,SUCCESS,EMPTY_VARS).emptyList(value.size()==0);
    }

    /**
     * Accept Collections and mark as empty or not
     * @param value
     * @return
     */
    public static<R> Result<Collection<R>> ok(Collection<R> value) {
        return new Result<Collection<R>>(value,OK,SUCCESS,EMPTY_VARS).emptyList(value.size()==0);
    }


    /**
     * Special Case for Void Type
     * @return
     */
    public static Result<Void> ok() {
        return new Result<Void>(null,OK,SUCCESS,EMPTY_VARS);
    }

    /**
     * Create a Status (usually non OK, with a details statement 
     * @param value
     * @param status
     * @param details
     * @return
     */
//    public static<R> Result<R> err(int status, String details) {
//        return new Result<R>(null,status,details,null);
//    }

    /**
     * Create a Status (usually non OK, with a details statement and variables supported
     * @param status
     * @param details
     * @param variables
     * @return
     */
    public static<R> Result<R> err(int status, String details, Object ... variables) {
        return new Result<R>(null,status,details,variables);
    }

    /**
     * Create Error from status and Details of previous Result (and not data)
     * @param pdr
     * @return
     */
    public static<R> Result<R> err(Result<?> pdr) {
        return new Result<R>(null,pdr.status,pdr.details,pdr.variables);
    }

    /**
     * Create General Error from Exception
     * @param e
     * @return
     */
    public static<R> Result<R> err(Exception e) {
        return new Result<R>(null,ERR_General,e.getMessage(),EMPTY_VARS);
    }

    /**
     * Create a Status (usually non OK, with a details statement 
     * @param value
     * @param status
     * @param details
     * @return
     */
    public static<R> Result<R> create(R value, int status, String details, Object ... vars) {
        return new Result<R>(value,status,details,vars);
    }

    /**
     * Create a Status from a previous status' result/details 
     * @param value
     * @param status
     * @param details
     * @return
     */
    public static<R> Result<R> create(R value, Result<?> result) {
        return new Result<R>(value,result.status,result.details,result.variables);
    }

    private static final int PARTIAL_CONTENT = 0x001;
    private static final int EMPTY_LIST = 0x002;

    /**
     * AAF Specific problems, etc 
     * <p>
     * @author Jonathan
     *
     */

    /**
     * specialCondition  is a bit field to enable multiple conditions, e.g. PARTIAL_CONTENT
     */
    private      int  specialCondition = 0;


    /**
     * Is result set only partial results, i.e. the DAO clipped the real result set to a smaller number.
     * @return  true iff result returned PARTIAL_CONTENT
     */
    public boolean partialContent() {
        return (specialCondition & PARTIAL_CONTENT) == PARTIAL_CONTENT;
    }

    /**
     * Set fact that result set only returned partial results, i.e. the DAO clipped the real result set to a smaller number.
     * @param hasPartialContent         set true iff result returned PARTIAL_CONTENT
     * @return   this Result object, so you can chain calls, in builder style
     */
    public Result<RV> partialContent(boolean hasPartialContent) {
        if (hasPartialContent) {
        specialCondition |= PARTIAL_CONTENT;
    } else {
        specialCondition &= (~PARTIAL_CONTENT);
    }
        return this;
    }

    /**
     * When Result is a List, you can check here to see if it's empty instead of looping
     * <p>
     * @return
     */
    public boolean isEmpty() {
        return (specialCondition & EMPTY_LIST) == EMPTY_LIST;
    }

    /**
     * A common occurrence is that data comes back, but list is empty.  If set, you can skip looking
     * at list at the outset.
     * <p>
     * @param emptyList
     * @return
     */
    public Result<RV> emptyList(boolean emptyList) {
        if (emptyList) {
            specialCondition |= EMPTY_LIST;
        } else {
            specialCondition &= (~EMPTY_LIST);
        }
        return this;
    }


    /** 
     * Convenience function.  Checks OK, and also if List is not Empty
     * Not valid if Data is not a List
     * @return
     */
    public boolean isOK() {
        return status == OK;
    }

    /** 
     * Convenience function.  Checks OK, and also if List is not Empty
     * Not valid if Data is not a List
     * @return
     */
    public boolean notOK() {
        return status != OK;
    }

    /** 
     * Convenience function.  Checks OK, and also if List is not Empty
     * Not valid if Data is not a List
     * @return
     */
    public boolean isOKhasData() {
        return status == OK && (specialCondition & EMPTY_LIST) != EMPTY_LIST;
    }


    /** 
     * Convenience function.  Checks OK, and also if List is not Empty
     * Not valid if Data is not a List
     * @return
     */
    public boolean notOKorIsEmpty() {
        return status != OK || (specialCondition & EMPTY_LIST) == EMPTY_LIST;
    }

    @Override
    public String toString() {
        if (status==0) {
            return details;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(status);
            sb.append(':');
            sb.append(String.format(details,((Object[])variables)));
            if (isEmpty()) {
                sb.append("{empty}");
            }
            if (value!=null) {
                sb.append('-');
                sb.append(value.toString());
            }
            return sb.toString();
        }
    }

    public String errorString() {
        StringBuilder sb = new StringBuilder();
        switch(status) {
            case 1: sb.append("Security"); break;
            case 2: sb.append("Denied"); break;
            case 3: sb.append("Policy"); break;
            case 4: sb.append("BadData"); break;
            case 5: sb.append("NotImplemented"); break;
            case 6: sb.append("NotFound"); break;
            case 7: sb.append("AlreadyExists"); break;
            case 8: sb.append("ActionNotComplete"); break;
            default: sb.append("Error");
        }
        sb.append(" - ");
        sb.append(String.format(details, variables));
        return sb.toString();
    }
}
