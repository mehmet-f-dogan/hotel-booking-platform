package dev.mehmetfd.common.context;

public class RequestContextHolder {

    private static final ThreadLocal<RequestContext> context = new ThreadLocal<>();

    public static void set(RequestContext requestContext) {
        context.set(requestContext);
    }

    public static RequestContext get() {
        return context.get();
    }

    public static RequestContext safeGet() {
        return context.get() != null ? context.get() : new RequestContext(null, null, null);
    }

    public static void clear() {
        context.remove();
    }
}