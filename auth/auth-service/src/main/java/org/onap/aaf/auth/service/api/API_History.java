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

package org.onap.aaf.auth.service.api;

import static org.onap.aaf.auth.layer.Result.OK;
import static org.onap.aaf.auth.rserv.HttpMethods.GET;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.onap.aaf.auth.dao.cass.Status;
import org.onap.aaf.auth.env.AuthzTrans;
import org.onap.aaf.auth.layer.Result;
import org.onap.aaf.auth.service.AAF_Service;
import org.onap.aaf.auth.service.Code;
import org.onap.aaf.auth.service.facade.AuthzFacade;
import org.onap.aaf.auth.service.mapper.Mapper.API;

/**
 * Pull certain types of History Info
 *
 * Specify yyyymm as
 *     single - 201504
 *  commas 201503,201504
 *  ranges 201501-201504
 *  combinations 201301,201401,201501-201504
 *
 * @author Jonathan
 *
 */
public class API_History {
    /**
     * Normal Init level APIs
     *
     * @param authzAPI
     * @param facade
     * @throws Exception
     */
    public static void init(AAF_Service authzAPI, AuthzFacade facade) throws Exception {
        /**
         * Get History
         */
        authzAPI.route(GET,"/authz/hist/user/:user",API.HISTORY,new Code(facade,"Get History by User", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                int[] years;
                int descend;
                try {
                    years = getYears(req);
                    descend = decending(req);
                } catch (Exception e) {
                    context.error(trans, resp, Result.err(Status.ERR_BadData, e.getMessage()));
                    return;
                }

                Result<Void> r = context.getHistoryByUser(trans, resp, pathParam(req,":user"),years,descend);
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        });

        /**
         * Get History by NS
         */
        authzAPI.route(GET,"/authz/hist/ns/:ns",API.HISTORY,new Code(facade,"Get History by Namespace", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                int[] years;
                int descend;
                try {
                    years = getYears(req);
                    descend = decending(req);
                } catch (Exception e) {
                    context.error(trans, resp, Result.err(Status.ERR_BadData, e.getMessage()));
                    return;
                }

                Result<Void> r = context.getHistoryByNS(trans, resp, pathParam(req,":ns"),years,descend);
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        });

        /**
         * Get History by Role
         */
        authzAPI.route(GET,"/authz/hist/role/:role",API.HISTORY,new Code(facade,"Get History by Role", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                int[] years;
                int descend;
                try {
                    years = getYears(req);
                    descend = decending(req);
                } catch (Exception e) {
                    context.error(trans, resp, Result.err(Status.ERR_BadData, e.getMessage()));
                    return;
                }

                Result<Void> r = context.getHistoryByRole(trans, resp, pathParam(req,":role"),years,descend);
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        });

        /**
         * Get History by Perm Type
         */
        authzAPI.route(GET,"/authz/hist/perm/:type",API.HISTORY,new Code(facade,"Get History by Perm Type", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                int[] years;
                int descend;
                try {
                    years = getYears(req);
                    descend = decending(req);
                } catch (Exception e) {
                    context.error(trans, resp, Result.err(Status.ERR_BadData, e.getMessage()));
                    return;
                }

                Result<Void> r = context.getHistoryByPerm(trans, resp, pathParam(req,":type"),years,descend);
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        });

        /**
         * Get History by Subject
         */
        authzAPI.route(GET,"/authz/hist/subject/:type/:subject",API.HISTORY,new Code(facade,"Get History by Perm Type", true) {
            @Override
            public void handle(AuthzTrans trans, HttpServletRequest req, HttpServletResponse resp) throws Exception {
                int[] years;
                int descend;
                try {
                    years = getYears(req);
                    descend = decending(req);
                } catch (Exception e) {
                    context.error(trans, resp, Result.err(Status.ERR_BadData, e.getMessage()));
                    return;
                }

                Result<Void> r = context.getHistoryBySubject(trans, resp, pathParam(req,":type"), pathParam(req,":subject"),years,descend);
                switch(r.status) {
                    case OK:
                        resp.setStatus(HttpStatus.OK_200);
                        break;
                    default:
                        context.error(trans,resp,r);
                }
            }
        });
    }

    // Check if Ascending
    private static int decending(HttpServletRequest req) {
        if ("true".equalsIgnoreCase(req.getParameter("desc")))return -1;
        if ("true".equalsIgnoreCase(req.getParameter("asc")))return 1;
        return 0;
    }

    // Get Common "yyyymm" parameter, or none

    private static int[] getYears(HttpServletRequest req) {
        // Sonar says threading issues.
        SimpleDateFormat FMT = new SimpleDateFormat("yyyyMM");
        String yyyymm = req.getParameter("yyyymm");
        ArrayList<Integer> ai= new ArrayList<>();
        if (yyyymm==null) {
            GregorianCalendar gc = new GregorianCalendar();
            // three months is the default
            for (int i=0;i<3;++i) {
                ai.add(Integer.parseInt(FMT.format(gc.getTime())));
                gc.add(GregorianCalendar.MONTH, -1);
            }
        } else {
            for (String ym : yyyymm.split(",")) {
                String range[] = ym.split("\\s*-\\s*");
                switch(range.length) {
                    case 0:
                        break;
                    case 1:
                        if (!ym.endsWith("-")) {
                            ai.add(getNum(ym));
                            break;
                        } else {
                            range=new String[] {ym.substring(0, 6),FMT.format(new Date())};
                        }
                    default:
                        GregorianCalendar gc = new GregorianCalendar();
                        gc.set(GregorianCalendar.MONTH, Integer.parseInt(range[1].substring(4,6))-1);
                        gc.set(GregorianCalendar.YEAR, Integer.parseInt(range[1].substring(0,4)));
                        int end = getNum(FMT.format(gc.getTime()));

                        gc.set(GregorianCalendar.MONTH, Integer.parseInt(range[0].substring(4,6))-1);
                        gc.set(GregorianCalendar.YEAR, Integer.parseInt(range[0].substring(0,4)));
                        for (int i=getNum(FMT.format(gc.getTime()));i<=end;gc.add(GregorianCalendar.MONTH, 1),i=getNum(FMT.format(gc.getTime()))) {
                            ai.add(i);
                        }

                }
            }
        }
        if (ai.size()==0) {
            throw new NumberFormatException(yyyymm + " is an invalid number or range");
        }
        Collections.sort(ai);
        int ym[] = new int[ai.size()];
        for (int i=0;i<ym.length;++i) {
            ym[i]=ai.get(i);
        }
        return ym;
    }

    private static int getNum(String n) {
        if (n==null || n.length()!=6) throw new NumberFormatException(n + " is not in YYYYMM format");
        return Integer.parseInt(n);
    }
}
