package com.example.ruby.getgps.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

/**
 * The type Mapbox helper.
 * <p/>
 * Mapbox static maps API: https://www.mapbox.com/developers/api/static/
 */
public class MapboxHelper {

    private static final String BASE_URL = "https://api.mapbox.com/v4/mapbox.streets/";
    private static final String START_PIN = "pin-s+30b59f";
    private static final String END_PIN = "pin-s+ff5166";
    private static final String PATH_SETTINGS = "path-8+2497C6-0.7+555555-0.0";
    private static final String IMAGE_FOCUS = "auto";
    private static final String EXTENSION = ".png";
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 300;
    private static final int MAX_POINTS = 700;

    /**
     * Build map url string.
     *
     * @param points      the points
     * @param startPin    the start pin
     * @param endPin      the end pin
     * @return the string
     */
    public static String buildMapUrl(List<LatLng> points, LatLng startPin, LatLng endPin) {
        return buildMapUrl(points, startPin, endPin, Constants.MAPBOX_API_KEY);
    }

    /**
     * Build map url string.
     *
     * @param points      the points
     * @param startPin    the start pin
     * @param endPin      the end pin
     * @param accessToken the access token
     * @return the string
     */
    private static String buildMapUrl(
            List<LatLng> points, LatLng startPin, LatLng endPin,
            String accessToken) {

        Timber.d("method=buildMapUrl pointsSizeBeforeCompression=%d pointsBeforeCompression='%s'", points.size(), Arrays.toString(points.toArray()));

        points = simplifyPath(points);
        Timber.d("method=buildMapUrl pointsSizeAfterCompression=%d pointsAfterCompression='%s'", points.size(), Arrays.toString(points.toArray()));

        String path = encodePath(points);
        Timber.d("method=buildMapUrl encodedPath='%s'", path);

        StringBuilder mapboxUrlBuilder = new StringBuilder(BASE_URL);
        mapboxUrlBuilder.append(PATH_SETTINGS)
                // encodedPath
                .append("(").append(path).append(")")
                // start pin
                .append(",").append(START_PIN)
                .append("(").append(startPin.longitude).append(",").append(startPin.latitude).append(")")
                // end pin
                .append(",").append(END_PIN)
                .append("(").append(endPin.longitude).append(",").append(endPin.latitude).append(")")
                // auto focus on map and image size
                .append("/").append(IMAGE_FOCUS)
                .append("/").append(MapboxHelper.DEFAULT_WIDTH).append("x").append(MapboxHelper.DEFAULT_HEIGHT)
                .append(EXTENSION)
                .append("?access_token=")
                .append(Constants.MAPBOX_API_KEY);

        Timber.d("method=buildMapUrl mapboxUrl='%s'", mapboxUrlBuilder.toString());
        return mapboxUrlBuilder.toString();
    }

    /**
     * Encode path string.
     *
     * @param points the points
     * @return the string
     */
    private static String encodePath(List<LatLng> points) {
        return PolyUtil.encode(points).replace("?", "%3F").replace("/", "%2F").replace("\\", "%5C");
    }

    /**
     * Zips all locations to have a number usable by Mapbox
     *
     * @param points allLocations
     * @return new list of latitudes and longitudes
     * <p/>
     * TODO: replace with Douglas–Peucker algorithm
     * https://en.wikipedia.org/wiki/Ramer–Douglas–Peucker_algorithm
     */
    private static List<LatLng> simplifyPath(List<LatLng> points) {
        if (points.size() <= MAX_POINTS) {
            return points;
        }

        // TODO: adjust the increment based on list size (if size is 701 we want
        // to remove 1 point instead of 350)
        int increment = 2;

        List<LatLng> newPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i += increment) {
            newPoints.add(points.get(i));
        }

        return simplifyPath(newPoints);
    }

}
