package com.carteleradiaria;

import java.util.ArrayList;
import java.util.List;

public class UploadObject {

  private List<Ciudad> ciudades = new ArrayList<>();

  public void addCiudad(String cityName) {
    this.ciudades.add(new Ciudad(cityName));
  }

}
