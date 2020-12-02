package de.ipvs.as.mbp.domain.entity_type;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.awt.image.BufferedImage;

@Component
public class EntityTypeValidator implements Validator {

    private static final long MIN_ALLOWED_ICON_SIZE = 20; //Bytes
    private static final long MAX_ALLOWED_ICON_SIZE = 5000000; //Bytes
    private static final int MIN_WIDTH = 10;
    private static final int MAX_WIDTH = 300;
    private static final int MIN_HEIGHT = 10;
    private static final int MAX_HEIGHT = 300;

    /**
     * Can this {@link Validator} {@link #validate(Object, Errors) validate}
     * instances of the supplied {@code clazz}?
     * <p>This method is <i>typically</i> implemented like so:
     * <pre class="code">return Foo.class.isAssignableFrom(clazz);</pre>
     * (Where {@code Foo} is the class (or superclass) of the actual
     * object instance that is to be {@link #validate(Object, Errors) validated}.)
     *
     * @param type the {@link Class} that this {@link Validator} is
     *             being asked if it can {@link #validate(Object, Errors) validate}
     * @return {@code true} if this {@link Validator} can indeed
     * {@link #validate(Object, Errors) validate} instances of the
     * supplied {@code clazz}
     */
    @Override
    public boolean supports(Class<?> type) {
        //Accept sub classes of EntityType
        return EntityType.class.isAssignableFrom(type);
    }

    /**
     * Validate the supplied {@code target} object, which must be
     * of a {@link Class} for which the {@link #supports(Class)} method
     * typically has (or would) return {@code true}.
     * <p>The supplied {@link Errors errors} instance can be used to report
     * any resulting validation errors.
     *
     * @param target the object that is to be validated (can be {@code null})
     * @param errors contextual state about the validation process (never {@code null})
     * @see ValidationUtils
     */
    @Override
    public void validate(Object target, Errors errors) {
        //Cast to entity type
        EntityType entityType = (EntityType) target;

        //Check if name was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "entity_type.name.empty",
                "The name must not be empty.");

        //Get icon
        EntityTypeIcon icon = entityType.getIcon();

        //Check if available
        if (icon == null) {
            errors.rejectValue("icon", "entity_type.icon.unavailable", "No icon provided.");
            return;
        }

        //Check icon size
        long size = icon.getSize();
        if ((size < MIN_ALLOWED_ICON_SIZE) || (size > MAX_ALLOWED_ICON_SIZE)) {
            errors.rejectValue("icon", "entity_type.icon.size", "The icon is of an invalid size.");
            return;
        }

        //Get image object from icon
        BufferedImage iconImage = icon.toImageObject();

        //Check for null
        if (iconImage == null) {
            errors.rejectValue("icon", "entity_type.icon.invalid", "Invalid icon provided.");
            return;
        }

        //Check icon dimensions (min, max and square)
        if ((iconImage.getWidth() < MIN_WIDTH) ||
                (iconImage.getWidth() > MAX_WIDTH) ||
                (iconImage.getHeight() < MIN_HEIGHT) ||
                (iconImage.getHeight() > MAX_HEIGHT) ||
                (iconImage.getWidth() != iconImage.getHeight())) {
            errors.rejectValue("icon", "entity_type.icon.dimensions",
                    "The icon must be a square and between " + MIN_WIDTH + "x" + MIN_HEIGHT +
                            " and " + MAX_WIDTH + "x" + MAX_HEIGHT + " pixels in size.");
        }
    }
}
