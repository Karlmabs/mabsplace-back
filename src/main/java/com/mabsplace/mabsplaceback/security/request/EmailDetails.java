package com.mabsplace.mabsplaceback.security.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailDetails implements Serializable {
  private String recipient;
  private String msgBody;
  private String subject;
  private String attachment;
}