package com.cyberwalkabout.foodtrucks.cities;

import java.util.List;

public interface CitiesProvider {

    boolean load();

    List<City> getCities();
}
