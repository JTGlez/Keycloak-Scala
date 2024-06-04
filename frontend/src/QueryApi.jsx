/* eslint-disable react/prop-types */
import { useState } from 'react';
import { Button, Card } from 'react-bootstrap';

const APIResponse = ({ response }) => {
    if (response) {
        return <pre>{response}</pre>;
    } else {
        return <div />;
    }
};

const QueryAPI = ({ keycloak }) => {
    const [response, setResponse] = useState(null);

    const authorizationHeader = () => {
        if (!keycloak) return {};
        return {
            headers: {
                "Authorization": "Bearer " + keycloak.token
            }
        };
    };

    const handleClick = () => {
        fetch('http://localhost:9000/users', authorizationHeader())
            .then(response => {
                if (response.status === 200)
                    return response.json();
                else
                    return { status: response.status, message: response.statusText };
            })
            .then(json => setResponse(JSON.stringify(json, null, 2)))
            .catch(err => setResponse(err.toString()));
    };

    return (
        <Card className="QueryAPI">
            <Card.Body>
                <Button variant="primary" onClick={handleClick}>
                    Send API request
                </Button>
                <APIResponse response={response} />
            </Card.Body>
        </Card>
    );
};

export default QueryAPI;
