package Model;

import java.util.Collection;
import java.util.HashMap;

public class ParcelMap {
    private HashMap<String, Parcel> parcels;

    public ParcelMap() {
        parcels = new HashMap<>();
    }

    public void addParcel(Parcel parcel) {
        parcels.put(parcel.getId(), parcel);
    }

    // Returns the Parcel if found, or null if not.
    public Parcel findParcel(String id) {
        return parcels.get(id);
    }

    // Check if a parcel with the given ID exists in the map.
    public boolean containsParcel(String id) {
        return parcels.containsKey(id);
    }

    // Removes the parcel with the specified ID.
    public void removeParcel(String id) {
        parcels.remove(id);
    }

    // Gets the days in depot for a parcel, returns -1 if the parcel doesn't exist.
    public int getDaysInDepot(String id) {
        Parcel parcel = findParcel(id);
        return parcel != null ? parcel.getDaysInDepot() : -1;
    }

    // Add this method to return all parcels
    public Collection<Parcel> getAllParcels() {
        return parcels.values();  // Returns the collection of all parcel values
}
}
