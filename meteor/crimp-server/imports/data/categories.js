import { Mongo } from 'meteor/mongo';
import { ValidatedMethod } from 'meteor/mdg:validated-method';
import { SimpleSchema } from 'meteor/aldeed:simple-schema';


class CategoriesCollection extends Mongo.collection {
  insert(message, callback) {
    return super.insert(message, callback);
  }
  update() {
    return false;
  }
  remove() {
    return false;
  }
}
