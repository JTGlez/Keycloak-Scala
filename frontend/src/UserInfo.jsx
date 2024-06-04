/* eslint-disable react/prop-types */
import { useState, useEffect } from 'react';
import { Card } from 'react-bootstrap';

export const UserInfo = ({ keycloak }) => {

    const [userInfo, setUserInfo] = useState({
        name: "",
        email: "",
        id: ""
    });

    useEffect(() => {
        keycloak.loadUserInfo().then(userInfo => {
            setUserInfo({
                name: userInfo.name,
                email: userInfo.email,
                id: userInfo.sub
            });
        });
    }, [keycloak]);


    return (
        <Card className="UserInfo">
            <Card.Body>
                <Card.Title>User Information</Card.Title>
                <Card.Text className="d-flex flex-column gap-2">
                    <span>Name: {userInfo.name}</span>
                    <span>Email: {userInfo.email}</span>
                    <span>ID: {userInfo.id}</span>
                </Card.Text>
            </Card.Body>
        </Card>
    );
}
