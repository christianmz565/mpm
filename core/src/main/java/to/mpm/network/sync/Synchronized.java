package to.mpm.network.sync;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un campo para que sea sincronizado automáticamente a través de la red
 * por {@link SyncedObject}.
 * <p>
 * Solo los campos anotados con esto serán considerados para la detección de
 * cambios y la propagación de actualizaciones.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Synchronized {
}
