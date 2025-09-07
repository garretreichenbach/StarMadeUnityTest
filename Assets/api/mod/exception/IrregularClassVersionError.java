package api.mod.exception;

/**
 * Created by Jake on 3/1/2021.
 * <insert description here>
 */
public class IrregularClassVersionError extends UnsupportedClassVersionError{
    public IrregularClassVersionError(String className, int version) {
        super("Most StarMade players use java 7, your class: " + className + " was compiled with major version(not java version): " + version);
    }
}
