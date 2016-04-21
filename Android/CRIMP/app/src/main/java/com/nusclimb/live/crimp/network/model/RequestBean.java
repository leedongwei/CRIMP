package com.nusclimb.live.crimp.network.model;

import java.io.Serializable;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class RequestBean implements Serializable{
    private static final long serialVersionUID = 1L;

    private PathBean pathBean;
    private QueryBean queryBean;
    private HeaderBean headerBean;
    private RequestBodyJs requestBodyJs;

    public PathBean getPathBean() {
        return pathBean;
    }

    public void setPathBean(PathBean pathBean) {
        this.pathBean = pathBean;
    }

    public QueryBean getQueryBean() {
        return queryBean;
    }

    public void setQueryBean(QueryBean queryBean) {
        this.queryBean = queryBean;
    }

    public HeaderBean getHeaderBean() {
        return headerBean;
    }

    public void setHeaderBean(HeaderBean headerBean) {
        this.headerBean = headerBean;
    }

    public RequestBodyJs getRequestBodyJs() {
        return requestBodyJs;
    }

    public void setRequestBodyJs(RequestBodyJs requestBodyJs) {
        this.requestBodyJs = requestBodyJs;
    }
}
