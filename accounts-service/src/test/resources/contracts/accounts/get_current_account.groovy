import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'returns current account for authenticated user'
    request {
        method GET()
        url '/api/accounts/me'
        headers {
            header 'Authorization': 'Bearer token'
            header 'X-User-Login': 'solovev'
        }
    }
    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body(
                login: 'solovev',
                firstName: 'Илья',
                lastName: 'Соловьев',
                birth_date: '1993-12-21',
                balance: 1000L
        )
    }
}