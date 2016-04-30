package rocks.crimp.crimp.service;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.UUID;

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
                                @Nullable Long climberId, @Nullable Long categoryId,
                                @Nullable Long routeId, @Nullable String markerId, long fbUserId,
                                @NonNull String fbAccessToken, long sequentialToken){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_GET_SCORE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID setActive(@NonNull Context context, @Nullable UUID txId, long fbUserId,
                                 @NonNull String fbAccessToken, long sequentialToken, long routeId,
                                 long climberId){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_SET_ACTIVE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID clearActive(@NonNull Context context, @Nullable UUID txId, long fbUserId,
                                   @NonNull String fbAccessToken, long sequentialToken,
                                   long routeId){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_CLEAR_ACTIVE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID login(@NonNull Context context, @Nullable UUID txId,
                             @NonNull String fbUserId, @NonNull String fbAccessToken,
                             boolean forceLogin){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_LOGIN);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID reportIn(@NonNull Context context, @Nullable UUID txId, String fbUserId,
                                @NonNull String fbAccessToken, long sequentialToken,
                                long categoryId, long routeId, boolean force){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_REPORT_IN);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID requestHelp(@NonNull Context context, @Nullable UUID txId, String fbUserId,
                                   @NonNull String fbAccessToken, long sequentialToken,
                                   long routeId){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_REQUEST_HELP);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID postScore(@NonNull Context context, @Nullable UUID txId, long routeId,
                                 long climberId, String fbUserId, @NonNull String fbAccessToken,
                                 long sequentialToken, @NonNull String score){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_POST_SCORE);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }

    @NonNull
    public static UUID logout(@NonNull Context context, @Nullable UUID txId, long fbUserId,
                              @NonNull String fbAccessToken, long sequentialToken){
        //TODO STUB
        if(txId == null){
            txId = UUID.randomUUID();
        }
        Intent intent = new Intent(context, CrimpService.class);
        intent.setAction(CrimpService.ACTION_LOGOUT);
        intent.putExtra(CrimpService.SERIALIZABLE_UUID, txId);
        context.startService(intent);

        return txId;
    }
}
