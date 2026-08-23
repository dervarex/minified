package com.dervarex.minified.launch.exceptions.loader;

import lombok.Getter;

@Getter
public class NoLoadersFoundException extends RuntimeException {

  /**
   * Loader as String in UPPERCASE, for example FABRIC or NEOFORGE
   */
  private final String loader;

  public NoLoadersFoundException(String message, String loader) {
    super(message);
    this.loader = loader;
  }

  public NoLoadersFoundException(String message, String loader, Throwable cause) {
    super(message, cause);
    this.loader = loader;
  }

}