package eu.sblendorio.bbs.tenants;

public class TheFoolBlog extends WordpressProxy {

    public TheFoolBlog() {
        super();
        this.logo = LOGO_BYTES;
        this.domain = "https://thefool.it";
        this.pageSize = 7;
        this.screenRows = 18;
    }

    private static final byte[] LOGO_BYTES = new byte[] {
        32, 5, -69, -84, 32, 32, 32, 32, 32, 18, -66, -69, -110, -69, 32, 32,
        32, 32, 32, 32, 18, -95, -110, -95, 32, -97, -84, -94, -94, 32, -94, 32,
        32, -84, -94, -69, 32, -94, -94, 13, 32, 18, 5, -84, -95, -110, -69, -84,
        18, -69, -110, -69, 32, 18, 32, -110, -94, 32, -94, -94, 32, -84, -94, -69,
        18, -95, -110, -95, 32, 18, -97, -95, -68, -66, -110, -66, 18, 32, -110, 32,
        32, 18, 32, -110, 32, 18, 32, -95, -110, -95, -84, -69, 13, 32, 5, -65,
        18, -95, -95, -110, -68, 18, -68, -110, 32, 32, 18, 32, -110, 32, 18, -95,
        -110, -95, 18, -95, -110, -95, 18, 32, -110, 32, 18, 32, -95, -110, -95, 32,
        18, -97, -95, -68, -66, -110, -66, 18, 32, -110, -94, -69, 18, -69, -110, -94,
        18, -84, -110, -68, 18, -68, -66, -110, -66, 13, 32, 32, 32, 32, 32, 32,
        32, 32, 18, 5, 32, -110, 32, -68, 18, -68, -66, -110, -66, 18, -69, -110,
        -94, 18, -84, -95, -110, -95, 32, -97, -84, -94, -94, -94, -94, -94, -94, -94,
        -94, -94, -94, -94, -94, -69, 13, 13
    };

}
