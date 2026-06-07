import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description 'returns current account for authenticated user'
    request {
        method GET()
        url '/api/accounts/me'
        headers {
            header 'Authorization': 'Bearer token'
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
                birthdate: '1993-12-21',
                sum: 1000
        )
    }
}