package cn.xxxl.chestnut.callback;

/**
 * @author Leon
 * @since 1.0.0
 */
public interface ChestnutCallback<T> {

    void succeed(T t);

    void failed(int code, T t);
}
