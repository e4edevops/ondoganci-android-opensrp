package org.smartregister.ondoganci.repository;

import java.util.List;

interface ClientRegisterTypeDao {
    List<ClientRegisterType> findAll(String baseEntityId);
    boolean remove(String registerType, String baseEntityId);
    boolean removeAll(String baseEntityId);
    boolean add(String registerType, String baseEntityId);
}
