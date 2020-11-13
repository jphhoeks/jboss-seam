package org.jboss.seam.captcha;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jboss.seam.core.Interpolator;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

/**
 * Validates that the input entered by the user matches
 * the captcha image.
 * 
 * @author Gavin King
 * @author Marek Novotny
 *
 */
public class CaptchaResponseValidator implements ConstraintValidator<CaptchaResponse, String> {

	private static final LogProvider log = Logging.getLogProvider(CaptchaResponseValidator.class);
	private CaptchaResponse annotation = null;

	public CaptchaResponseValidator() {
		super();
	}

	@Override
	public void initialize(CaptchaResponse constraintAnnotation) {
		annotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		boolean result = Captcha.instance().validateResponse(value);
		if (!result) {

			context.disableDefaultConstraintViolation();
			if (log.isDebugEnabled()) {
				log.debug("annotation.message=" + annotation.message());
			}
			String template = Interpolator.instance().interpolate(annotation.message());
			context.buildConstraintViolationWithTemplate(template).addConstraintViolation();
		}
		return result;
	}

}
