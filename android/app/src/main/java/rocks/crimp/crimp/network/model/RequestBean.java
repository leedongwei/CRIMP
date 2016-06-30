package rocks.crimp.crimp.network.model;

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
    private MetaBean metaBean;

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

    public MetaBean getMetaBean() {
        return metaBean;
    }

    public void setMetaBean(MetaBean metaBean) {
        this.metaBean = metaBean;
    }
}
