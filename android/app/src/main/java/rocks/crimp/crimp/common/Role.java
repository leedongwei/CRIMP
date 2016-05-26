package rocks.crimp.crimp.common;

/**
 * @author Lin Weizhi (ecc.weizhi@gmail.com)
 */
public enum Role {
    DENIED("denied"),
    PENDING("pending"),
    PARTNER("partner"),
    JUDGE("judge"),
    ADMIN("admin"),
    HUKKATAIVAL("hukkataival");

    private final String value;

    Role(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }

    public Role toEnum(String value){
        switch(value){
            case "denied":
                return DENIED;
            case "pending":
                return PENDING;
            case "partner":
                return PARTNER;
            case "judge":
                return JUDGE;
            case "admin":
                return ADMIN;
            case "hukkataival":
                return HUKKATAIVAL;
            default:
                return null;
        }
    }
}
