/* eslint-disable react/prop-types */
import { useNavigate } from 'react-router-dom';
import { Button } from 'react-bootstrap';

export const Logout = ({ keycloak }) => {
    const navigate = useNavigate();

    const logout = () => {
        const idToken = keycloak.tokenParsed.id_token;
        keycloak.logout({
            post_logout_redirect_uri: 'http://localhost:5173',
            id_token_hint: idToken
        }).then(() => {
            navigate('/');
        });
    };

    return (
        <Button variant="primary" onClick={logout}>
            Logout
        </Button>
    );
};
