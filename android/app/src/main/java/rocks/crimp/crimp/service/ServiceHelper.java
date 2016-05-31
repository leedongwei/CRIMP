package rocks.crimp.crimp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

import rocks.crimp.crimp.network.model.HeaderBean;
import rocks.crimp.crimp.network.model.MetaBean;
import rocks.crimp.crimp.network.model.PathBean;
import rocks.crimp.crimp.network.model.QueryBean;
import rocks.crimp.crimp.network.model.RequestBean;
import rocks.crimp.crimp.network.model.RequestBodyJs;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class ServiceHelper {
    private static ServiceHelper instance;

    private ServiceHelper(){

    }

    public static ServiceHelper getInstance(){
        if(instance == null){
            instance = new ServiceHelper();
        }
        return instance;
    }

    @NonNull
    public static UUID getCategories(@NonNull Context context, @Nullable UUID txId){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_GET_CATEGORIES);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID getScore(@NonNull Context context, @Nullable UUID txId,
                                @Nullable String climberId, @Nullable String categoryId,
                                @Nullable String routeId, @Nullable String markerId,
                                @NonNull String xUserId, @NonNull String xAuthToken){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        requestBean.setHeaderBean(header);
        QueryBean query = new QueryBean();
        query.setClimberId(climberId);
        query.setCategoryId(categoryId);
        query.setRouteId(routeId);
        query.setMarkerId(markerId);
        requestBean.setQueryBean(query);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_GET_SCORE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID setActive(@NonNull Context context, @Nullable UUID txId,
                                 @NonNull String xUserId, @NonNull String xAuthToken,
                                 @NonNull String routeId, @NonNull String markerId){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        requestBean.setHeaderBean(header);
        RequestBodyJs body = new RequestBodyJs();
        body.setRouteId(routeId);
        body.setMarkerId(markerId);
        requestBean.setRequestBodyJs(body);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_SET_ACTIVE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID clearActive(@NonNull Context context, @Nullable UUID txId,
                                   @NonNull String xUserId, @NonNull String xAuthToken,
                                   @NonNull String routeId){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        requestBean.setHeaderBean(header);
        RequestBodyJs body = new RequestBodyJs();
        body.setRouteId(routeId);
        requestBean.setRequestBodyJs(body);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_CLEAR_ACTIVE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID login(@NonNull Context context, @Nullable UUID txId,
                             @NonNull String fbAccessToken){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        RequestBodyJs body = new RequestBodyJs();
        body.setFbAccessToken(fbAccessToken);
        requestBean.setRequestBodyJs(body);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_LOGIN);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID reportIn(@NonNull Context context, @Nullable UUID txId,
                                @NonNull String xUserId, @NonNull String xAuthToken,
                                @NonNull String categoryId, @NonNull String routeId,
                                boolean force){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        RequestBodyJs body = new RequestBodyJs();
        body.setCategoryId(categoryId);
        body.setRouteId(routeId);
        body.setForceReport(force);
        requestBean.setHeaderBean(header);
        requestBean.setRequestBodyJs(body);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_REPORT_IN);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID requestHelp(@NonNull Context context, @Nullable UUID txId,
                                   @NonNull String xUserId, @NonNull String xAuthToken,
                                   @Nullable String routeId){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        requestBean.setHeaderBean(header);
        RequestBodyJs body = new RequestBodyJs();
        body.setRouteId(routeId);
        requestBean.setRequestBodyJs(body);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_REQUEST_HELP);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID postScore(@NonNull Context context, @Nullable UUID txId,
                                 @NonNull String routeId, @NonNull String markerId,
                                 @NonNull String xUserId, @NonNull String xAuthToken,
                                 @NonNull String score, @NonNull String categoryName,
                                 @NonNull String routeName){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        RequestBodyJs body = new RequestBodyJs();
        body.setScoreString(score);
        PathBean path = new PathBean();
        path.setRouteId(routeId);
        path.setMarkerId(markerId);
        MetaBean meta = new MetaBean();
        meta.setCategoryName(categoryName);
        meta.setRouteName(routeName);
        requestBean.setHeaderBean(header);
        requestBean.setRequestBodyJs(body);
        requestBean.setPathBean(path);
        requestBean.setMetaBean(meta);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_POST_SCORE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID logout(@NonNull Context context, @Nullable UUID txId,
                              @NonNull String xUserId, @NonNull String xAuthToken){
        if(txId == null){
            txId = UUID.randomUUID();
        }
        RequestBean requestBean = new RequestBean();
        HeaderBean header = new HeaderBean();
        header.setxUserId(xUserId);
        header.setxAuthToken(xAuthToken);
        requestBean.setHeaderBean(header);

        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_LOGOUT);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        intent.putExtra(CrimpService.SERIALIZABLE_REQUEST, requestBean);
        context.startService(intent);

        return txId;
    }
}
