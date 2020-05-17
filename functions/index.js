const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Notification to the owner of a item
exports.sendFavoriteNotification = functions.firestore
  .document('favorites/{favoriteID}')
  .onCreate((snap, context) => {
    // Get an object representing the document
    const newValue = snap.data();
    const uuid = newValue.seller;

    console.log('User to send notification', uuid);

    var ref = admin.firestore()
                .collection("users")
                .doc(uuid)
                .get()
                .then(doc => {
                  const payload = {
                    notification: {
                      title: 'Qualcuno è interessato ad un tuo annuncio.',
                      body: doc.data().nickname+' un utente è interessato al tuo annuncio!'
                    }
                  };

                  console.log("Token value: ", doc.data().token)
                  admin.messaging().sendToDevice(doc.data().token, payload)

                  return true
                })
  
  });


function sendDataToUser(userid, data){
  const queryToken = admin.firestore()
    .collection("users")
    .doc(userid);

   queryToken.get()
      .then(doc =>{
        console.log("Sending message to user with token: ", doc.data().token)
        admin.messaging().sendToDevice(doc.data().token, data)

        return true;
      }).catch(error => {
        console.log("Error: ", error)
      });
  
}

  exports.sendItemChangeNotification = functions.firestore
    .document('items/{itemID}')
    .onUpdate((change, context) => {
      
      const newItem = change.after.data();
      const prevItem = change.before.data();
      const itemID = context.params.itemID;
    
      // Handle state change
      if(newItem.state !== prevItem.state){
        const query = admin.firestore()
          .collection('favorites')
          .where('item', '==', itemID);

        query.get().then(querySnapshot => {
          querySnapshot.docs.map(doc => {
            const buyer = doc.data().user

            console.log("Item change, sending data to", buyer)

            const payload = {
              notification: {
                title: 'Aggiornamento annuncio',
                body: 'L\'oggetto '+newItem.name+' ha cambiato lo stato in: '+newItem.state
              }
            };

            sendDataToUser(buyer, payload)
            
            return true
          })
          return true
        }).catch(error =>{
          console.log("Error: ", error)
        })
        
      }

      return true
});