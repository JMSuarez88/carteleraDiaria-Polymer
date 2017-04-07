package com.carteleradiaria;

import java.util.ArrayList;
import java.util.List;

public class Ciudad {

  private String name;
  private List<Sede> sedes = new ArrayList<>();

  public Ciudad(String name) {
    this.name = name;
  }

  public void setSedes(List<Sede> sedes) {
    this.sedes = sedes;
  }
}
