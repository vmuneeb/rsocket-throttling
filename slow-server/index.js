const express = require('express')
const app = express()
const port = 3000

app.get('/:userId', function(req, res){
    console.log(`Request received to cancel `, req.params.userId)
    setTimeout(() => res.send(req.params.userId),1000*5)
})

app.listen(port, () => console.log(`Example app listening at http://localhost:${port}`))
