package rocks.crimp.crimp.common;

import android.support.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import rocks.crimp.crimp.CrimpApplication;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public class Helper {
    public static boolean isJudgeOrAbove(@Nullable Set<String> rolesSet){
        if(rolesSet == null){
            return false;
        }

        Set<String> tempSet = new HashSet<>(rolesSet);
        tempSet.remove(Role.DENIED.getValue());
        tempSet.remove(Role.PENDING.getValue());
        tempSet.remove(Role.PARTNER.getValue());
        return tempSet.size() > 0;
    }

    public static void assertStuff(Boolean hasAccessToken, Boolean hasUserName,
                                   Boolean hasXUserId, Boolean hasXAuthToken, Boolean hasRoles){
        if(hasAccessToken != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.FB_ACCESS_TOKEN) != hasAccessToken){
                throw new IllegalStateException("Fb access token");
            }
        }

        if(hasUserName != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.FB_USER_NAME) != hasUserName){
                throw new IllegalStateException("Fb user name");
            }
        }

        if(hasXUserId != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.X_USER_ID) != hasXUserId){
                throw new IllegalStateException("X-User-Id");
            }
        }

        if(hasXAuthToken != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.X_AUTH_TOKEN) != hasXAuthToken){
                throw new IllegalStateException("X-Auth-Token");
            }
        }

        if(hasRoles != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.ROLES) != hasRoles){
                throw new IllegalStateException("roles");
            }
        }
    }

    public static void assertStuff(Boolean hasAccessToken, Boolean hasUserName,
                                   Boolean hasXUserId, Boolean hasXAuthToken,
                                   Boolean hasRoles, Boolean hasCanDisplay,
                                   Boolean hasMarkerId, Boolean hasClimberName,
                                   Boolean hasShouldScan, Boolean hasCommittedCategory,
                                   Boolean hasCommittedRoute, Boolean hasCategoryPosition,
                                   Boolean hasRoutePosition, Boolean hasCurrentScore,
                                   Boolean hasAccumulatedScore, //Boolean hasMarkerIdTemp,
                                   Boolean hasImageHeight){
        assertStuff(hasAccessToken, hasUserName, hasXUserId, hasXAuthToken, hasRoles);

        if(hasCanDisplay != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.CAN_DISPLAY) != hasCanDisplay){
                throw new IllegalStateException("canDisplay");
            }
        }

        if(hasMarkerId != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.MARKER_ID) != hasMarkerId){
                throw new IllegalStateException("marker id");
            }
        }

        if(hasClimberName != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.CLIMBER_NAME) != hasClimberName){
                throw new IllegalStateException("climber name");
            }
        }

        if(hasShouldScan != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.SHOULD_SCAN) != hasShouldScan){
                throw new IllegalStateException("should scan");
            }
        }

        if(hasCommittedCategory != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.COMMITTED_CATEGORY) != hasCommittedCategory){
                throw new IllegalStateException("committed category");
            }
        }

        if(hasCommittedRoute != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.COMMITTED_ROUTE) != hasCommittedRoute){
                throw new IllegalStateException("committed route");
            }
        }

        if(hasCategoryPosition != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.CATEGORY_POSITION) != hasCategoryPosition){
                throw new IllegalStateException("category position");
            }
        }

        if(hasRoutePosition != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.ROUTE_POSITION) != hasRoutePosition){
                throw new IllegalStateException("route position");
            }
        }

        if(hasCurrentScore != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.CURRENT_SCORE) != hasCurrentScore){
                throw new IllegalStateException("current score");
            }
        }

        if(hasAccumulatedScore != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.ACCUMULATED_SCORE) != hasAccumulatedScore){
                throw new IllegalStateException("accumulated score");
            }
        }

        /*
        if(hasMarkerIdTemp != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.MARKER_ID_TEMP) != hasMarkerIdTemp){
                throw new IllegalStateException("marker id temp");
            }
        }
        */

        if(hasImageHeight != null){
            if(CrimpApplication.getAppState().contains(CrimpApplication.IMAGE_HEIGHT) != hasImageHeight){
                throw new IllegalStateException("image height");
            }
        }
    }
}
