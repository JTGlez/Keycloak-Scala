/* eslint-disable no-unused-vars */
/* eslint-disable no-unused-vars */
import React, { useState, useEffect } from 'react';
import Keycloak from 'keycloak-js';
import { UserInfo } from './UserInfo';
import { Logout } from './Logout';

export const SecuredApp = () => {
    const [keycloak, setKeycloak] = useState(null);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        const keycloakInstance = Keycloak('/keycloak.json');
        keycloakInstance.init({ onLoad: 'login-required' }).then(authenticated => {
            setKeycloak(keycloakInstance);
            setAuthenticated(authenticated);
        }).catch(error => {
            console.error('Failed to initialize Keycloak', error);
        });
    }, []);

    if (keycloak) {
        if (authenticated) {
            return (
                <div>
                    <span>
                        This is a Keycloak-secured component of your application. You shouldnt be able
                        to see this unless you ve authenticated with Keycloak.
                    </span>
                    <UserInfo keycloak={keycloak} />
                    <Logout keycloak={keycloak} />
                </div>
            );
        } else {
            return <div>Unable to authenticate!</div>;
        }
    }

    return <div>Initializing Keycloak...</div>;
};
