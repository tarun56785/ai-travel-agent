import Keycloak from 'keycloak-js';

// We tell the adapter exactly where the Front Desk is located
const keycloak = new Keycloak({
  url: 'http://127.0.0.1:9080/',
  realm: 'concierge-realm',
  clientId: 'react-app'
});

export default keycloak;