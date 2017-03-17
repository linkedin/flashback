/*
 * Copyright (c) LinkedIn Corporation. All rights reserved. Licensed under the BSD-2 Clause license.
 * See LICENSE in the project root for license information.
 */

package com.linkedin.flashback.serializable;

import java.io.IOException;


/**
 *Interface of Recorded http body
 * @author shfeng
 */
public interface RecordedHttpBody {
  /**
   * convert http body to content byte array to be sent over the wire
   * @param charSet  charset
   * */
  byte[] getContent(String charSet)
      throws IOException;
}
