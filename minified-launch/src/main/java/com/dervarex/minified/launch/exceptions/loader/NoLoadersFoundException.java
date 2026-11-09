package com.dervarex.minified.launch.exceptions.loader;

import lombok.Getter;
import org.apiguardian.api.API;

/**
 * Gets thrown when no loaders could be found (loader exists, but no versions found)
 */
@Getter
@API(status = API.Status.STABLE)
public class NoLoadersFoundException extends RuntimeException {

  /**
   * Loader as String in UPPERCASE, for example FABRIC or NEOFORGE
   */
  private final String loader;

  /**
   * Creates the exception
   *
   * @param message what went wrong
   * @param loader  the loader in uppercase, like FABRIC
   */
  public NoLoadersFoundException(String message, String loader) {
    super(message);
    this.loader = loader;
  }

  /**
   * Creates the exception
   *
   * @param message what went wrong
   * @param loader  the loader in uppercase, like FABRIC
   * @param cause   the original error
   */
  public NoLoadersFoundException(String message, String loader, Throwable cause) {
    super(message, cause);
    this.loader = loader;
  }

}