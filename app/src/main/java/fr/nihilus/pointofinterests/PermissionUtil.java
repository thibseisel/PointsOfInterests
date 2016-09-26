package fr.nihilus.pointofinterests;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Classe utilitaire permettant de gérer les runtime permissions
 * introduites dans Android Marshmallow.
 */
public class PermissionUtil {

    /**
     * Indique si l'application a la permission d'utiliser les services de localisation.
     */
    public static boolean hasLocationPermissions(Context ctx) {
        return ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(ctx,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Demande la permission à l'utilisateur d'utiliser les services de localisation
     * pour cette application.
     * Le résultat de cette requête est envoyé à la méthode
     * {@link Activity#onRequestPermissionsResult(int, String[], int[])}
     * @param activity activité demandant la permission
     * @param requestCode code à utiliser pour vérifier que la permission a été accordée ou non
     */
    public static void requestLocationPermissions(Activity activity, int requestCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
    }
}
