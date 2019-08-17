-- access_token_validity is one hour
-- db_client password is 'db_client_password'
INSERT INTO oauth_client_details
	(client_id, client_secret, scope, authorized_grant_types,
	web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity,
	additional_information, autoapprove, resource_ids)
VALUES
('db_client',
 '$2a$07$4PH.JHQngyRyq7yrx0yw6uURVCA7erVbrKrCHOxXwwgzdEPx.u/y2',
 'read,write',
 'authorization_code,password,refresh_token',
 'http://localhost:8080/login/demo-db',
 null,
 10,
 120,
 null,
 false,
 'idp-db-resource,resource-jwt-greeting,resource-jwt-echo')
ON CONFLICT DO NOTHING;

-- client that is actually a resource server and uses /oauth/check_token to decode tokens
-- see OAuth2ServerConfiguration - AuthorizationServerSecurityConfigurer - checkTokenAccess
INSERT INTO oauth_client_details
	(client_id, client_secret, authorities, access_token_validity, autoapprove)
VALUES
('check_token_client',
 '$2y$07$lhPiItllYTQeuV6o4jn99OOtWXP7SeuVVIfB9NV2.1IJXAGdtebwi',
 'ROLE_TRUSTED_CLIENT',
 0,
 false)
ON CONFLICT DO NOTHING;
