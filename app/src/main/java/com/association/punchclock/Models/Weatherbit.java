package com.association.punchclock.Models;

public class Weatherbit {
//      default;   //english, Celcius, m/s, mm,
        public class Weather {
//                 https://www.weatherbit.io/api/codes
                public String icon; //Weather icon code. https://www.weatherbit.io/static/img/icons/icon-name.png
                public String code; //Weather code.
                public String description;   //Text weather description.
        }

        public String lat;   //Latitude (Degrees).
        public String lon;   //Longitude (Degrees).
        public String sunrise;   //Sunrise time (HH:MM).
        public String sunset;   //Sunset time (HH:MM).
        public String timezone;   //Local IANA Timezone.
        public String station;   //Source station ID.
        public String ob_time;   //Last observation time (YYYY-MM-DD HH:MM).
        public String datetime;   //Current cycle hour (YYYY-MM-DD:HH).
        public String ts;   //Last observation time (Unix timestamp).
        public String city_name;   //City name.
        public String country_code;   //Country abbreviation.
        public String state_code;   //State abbreviation/code.
        public String pres;   //Pressure (mb).
        public String slp;   //Sea level pressure (mb).
        public String wind_spd;   //Wind speed (Default m/s).
        public String wind_dir;   //Wind direction (degrees).
        public String wind_cdir;   //Abbreviated wind direction.
        public String wind_cdir_full;   //Verbal wind direction.
        public String temp;   //Temperature (default Celcius).
        public String app_temp;   //Apparent/"Feels Like" temperature (default Celcius).
        public String rh;   //Relative humidity (%).
        public String dewpt;   //Dew point (default Celcius).
        public String clouds;   //Cloud coverage (%).
        public String pod;   //Part of the day (d = day / n = night).
        public Weather weather;
        public String vis;   //Visibility (default KM).
        public String precip;   //Liquid equivalent precipitation rate (default mm/hr).
        public String snow;   //Snowfall (default mm/hr).
        public String uv;   //UV Index (0-11+).
        public String aqi;   //Air Quality Index [US - EPA standard 0 - +500]
        public String dhi;   //Diffuse horizontal solar irradiance (W/m^2) [Clear Sky]
        public String dni;   //Direct normal solar irradiance (W/m^2) [Clear Sky]
        public String ghi;   //Global horizontal solar irradiance (W/m^2) [Clear Sky] http://rredc.nrel.gov/solar/pubs/shining/chap4.html
        public String solar_rad;   //Estimated Solar Radiation (W/m^2).
        public String elev_angle;   //Solar elevation angle (degrees).
        public String h_angle;   //Solar hour angle (degrees).
        public String weatherImage(){
                return "https://www.weatherbit.io/static/img/icons/"+this.weather.icon+".png";
        }
        public String Pressure(){
                float preasure = Float.parseFloat(pres) / 10;
                preasure = (int) (preasure*100)/100;
                return String.valueOf(preasure);
        }

}
