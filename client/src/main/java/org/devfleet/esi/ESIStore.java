package org.devfleet.esi;

public interface ESIStore {

    void save(final ESIToken token);

    void delete(final String refresh);

    ESIToken get(final String refresh);

}
