# Agama-Inji-Wallet

[![Contributors][contributors-shield]](contributors-url)
[![Forks][forks-shield]](forks-url)
[![Stargazers][stars-shield]](stars-url)
[![Issues][issues-shield]](issues-url)
[![Apache License][license-shield]](license-url)


## Overview

Agama-Inji-Wallet is an authentication flow project that integrates [MOSIP Inji Wallet](https://injiweb.collab.mosip.net/) with Janssen authentication server. This project enables users to authenticate and register using verifiable credentials from their Inji digital wallet, specifically National ID (NID) credentials

The flow validates credentials through the Inji Verify backend, extracts user information from the verified credential, and creates user accounts in the Janssen server with password setup.

## Features

- **Verifiable Credential Authentication**: Validates NID credentials from MOSIP Inji Wallet
- **OpenID4VP Integration**: Uses OpenID for Verifiable Presentations protocol
- **User Onboarding**: Automatically creates user accounts from verified credential data
- **Verifiable Credentials Storage**: Stores complete verifiable credentials as JSONB in database
- **Intelligent Credential Type Detection**: Automatically identifies credential types (NID, TAX, etc.) based on content
- **Credential Merging**: Supports adding multiple credential types to existing users
- **Editable User Information**: Users can review and edit verified information before registration
- **Password Setup**: Allows users to set passwords during first-time registration
- **Existing User Detection**: Recognizes returning users and logs them in directly
- **Credential Management**: Remove specific credential types while preserving others
- **Extensible Credential Support**: Architecture supports multiple credential types (NID, TAX info, etc.)
- **Configurable Attribute Mapping**: Maps credential claims to Janssen user attributes

## Flow Sequence

1. **Verification Request**: Agama flow creates a VP (Verifiable Presentation) verification request
2. **Authorization URL**: Constructs OpenID4VP authorization URL for Inji Web
3. **RFAC Call**: Redirects user to Inji Web wallet application
4. **Credential Presentation**: User presents their NID credential from wallet
5. **Verification**: Backend validates the credential and transaction
6. **Credential Storage**: System stores complete verifiable credentials in JSONB format
7. **User Check**: System checks if user already exists by email
   - **Existing User**: Merges new credentials with existing ones, logs in directly
   - **New User**: Shows setup page with editable fields
8. **Profile Setup**: New users review NID data, can edit fields, and set password
9. **Account Creation**: User account created with verified information and stored credentials
10. **Authentication Complete**: User logged into Janssen

## Where To Deploy

The project can be deployed to any IAM server that runs an implementation of 
the [Agama Framework](https://docs.jans.io/head/agama/introduction/) like 
[Janssen Server](https://jans.io) and [Gluu Flex](https://gluu.org/flex/).

## How To Deploy

Different IAM servers may provide different methods and 
user interfaces from where an Agama project can be deployed on that server. 
The steps below show how the **Agama-Inji-Wallet** project can be deployed on the 
[Janssen Server](https://jans.io). 

Deployment of an Agama project involves three steps

- [Downloading the `.gama` package from project repository](#download-the-project)
- [Adding the `.gama` package to the IAM server](#add-the-project-to-the-server)
- [Configure the project](#configure-the-project)

### Download the Project

> [!TIP]
> Skip this step if you use the Janssen Server TUI tool to 
> configure this project. The TUI tool enables the download and adding of this 
> project directly from the tool, as part of the `community projects` listing. 

The project is bundled as 
[.gama package](https://docs.jans.io/head/agama/gama-format/). 
Visit the `Assets` section of the 
[Releases](https://github.com/GluuFederation/agama-inji-wallet/releases) to download 
the `.gama` package.

### Add The Project To The Server

 The Janssen Server provides multiple ways an Agama project can be 
 deployed and configured. Either use the command-line tool, REST API, or a 
 TUI (text-based UI). Refer to 
 [Agama project configuration page](https://docs.jans.io/head/admin/config-guide/auth-server-config/agama-project-configuration/) in the Janssen Server documentation for more 
 details.

### Configure The Project

Agama project accepts configuration parameters in the JSON format. Every Agama 
project comes with a basic sample configuration file for reference.

Below is a typical configuration of the **Agama-Inji-Wallet** project. As show, it contains
Edit the configuration through Janssen TUI:
   ```json
   {
     "injiWebBaseURL": "https://injiweb.collab.mosip.net",
     "injiVerifyBaseURL": "https://injiverify.collab.mosip.net",
     "agamaCallBackUrl": "https://your-janssen-server.com/jans-auth/fl/callback",
     "clientId": "your-client-id",
     "credentialMappings": [...]
   }
   ```

### Credential Mappings

The `credentialMappings` array defines how credential claims map to Janssen user attributes:

```json
"credentialMappings": [
  {
    "credentialType": "NID",
    "vcToGluuMapping": {
      "fullName": "displayName",
      "phone": "mobile",
      "gender": "gender",
      "email": "mail",
      "dateOfBirth": "birthdate"
    }
  }
]
```

**Credential Claim** (left) → **Janssen Attribute** (right)

### Presentation Definition

Defines the credential requirements for verification:

```json
"presentationDefinition": {
  "id": "unique-id",
  "purpose": "Authentication purpose description",
  "format": {
    "ldp_vc": {
      "proof_type": ["Ed25519Signature2020"]
    }
  },
  "input_descriptors": [...]
}
```

## Extending for Multiple Credential Types

The project supports multiple credential types. To add TAX credential support:

```json
"credentialMappings": [
  {
    "credentialType": "NID",
    "vcToGluuMapping": {
      "fullName": "displayName",
      "email": "mail",
      ...
    }
  },
  {
    "credentialType": "TAX",
    "vcToGluuMapping": {
      "taxId": "taxIdentifier",
      "income": "annualIncome",
      "email": "mail",
      ...
    }
  }
]
```

Currently, the flow uses the first credential mapping (NID). Future enhancements can add credential type selection.

## Verifiable Credentials Storage

### Database Schema

The project stores complete verifiable credentials in a JSONB column. Following [docs](https://docs.jans.io/head/janssen-server/reference/database/pgsql-ops/#add-custom-attribute)

```sql
ALTER TABLE "jansPerson" ADD COLUMN "verifiableCredentials" jsonb;
```



<!-- This are stats url reference for this repository -->
[contributors-shield]: https://img.shields.io/github/contributors/GluuFederation/agama-inji-wallet.svg?style=for-the-badge
[contributors-url]: https://github.com/GluuFederation/agama-inji-wallet/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/GluuFederation/agama-inji-wallet.svg?style=for-the-badge
[forks-url]: https://github.com/GluuFederation/agama-inji-wallet/network/members
[stars-shield]: https://img.shields.io/github/stars/GluuFederation/agama-inji-wallet?style=for-the-badge
[stars-url]: https://github.com/GluuFederation/agama-inji-wallet/stargazers
[issues-shield]: https://img.shields.io/github/issues/GluuFederation/agama-inji-wallet.svg?style=for-the-badge
[issues-url]: https://github.com/GluuFederation/agama-inji-wallet/issues
[license-shield]: https://img.shields.io/github/license/GluuFederation/agama-inji-wallet.svg?style=for-the-badge
[license-url]: https://github.com/GluuFederation/agama-inji-wallet/blob/main/LICENSE
