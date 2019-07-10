-- access_token_validity is one hour
-- db_client password is 'db_client_password'
INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity,
	additional_information, autoapprove, resource_ids)
VALUES
('db_client',
 '$2a$07$4PH.JHQngyRyq7yrx0yw6uURVCA7erVbrKrCHOxXwwgzdEPx.u/y2',
 'read,write',
 'authorization_code,password',
 'http://localhost:8080/login/demo-db',
 null,
 3600,
 null,
 false,
 'idp-db-resource,resource-jwt-greeting')
ON CONFLICT DO NOTHING;

-- client that is actually a resource server and uses /oauth/check_token to decode tokens
-- see OAuth2ServerConfiguration - AuthorizationServerSecurityConfigurer - checkTokenAccess
INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity,
	additional_information, autoapprove, resource_ids)
VALUES
('check_token_client',
 'todo',
 null,
 null,
 null,
 'ROLE_TRUSTED_CLIENT',
 0,
 null,
 false,
 null)
ON CONFLICT DO NOTHING;
