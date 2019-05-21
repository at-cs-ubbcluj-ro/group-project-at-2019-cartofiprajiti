import base64
import json
from io import BytesIO

import cv2
import numpy as np
from PIL import Image
from flask import Flask, Response
import requests
from flask_cors import CORS


def get_machine_learning_model():
    """
    Load the machine learning model using cv2.
    :return: the machine learning model.
    reference: https://www.pyimagesearch.com/2018/11/12/yolo-object-detection-with-opencv/
    """
    weights_path = "yolov3.weights"
    config_path = "yolov3.cfg"
    net = cv2.dnn.readNetFromDarknet(config_path, weights_path)
    return net


def get_num_person_predictions(layer_outputs):
    """
    Return the number of persons from the output layer(final prediction).
    :param layer_outputs: the output layer of the neural network
    :return: the number of persons
    """
    num_persons = 0
    threshold = 0.5
    person_id = 0
    for output in layer_outputs:
        for detection in output:
            scores = detection[5:]
            class_id = np.argmax(scores)
            confidence = scores[class_id]
            # we only increment the counter if we are satisfied with the net's confidence
            # and if the current prediction represents a 'person'
            if confidence >= threshold and class_id == person_id:
                num_persons += 1

    return num_persons


def get_number_of_persons(model, image):
    """
    Returns the number of persons from an image.
    reference: https://www.pyimagesearch.com/2018/11/12/yolo-object-detection-with-opencv/
    """
    ln = model.getLayerNames()
    ln = [ln[i[0] - 1] for i in model.getUnconnectedOutLayers()]
    blob = cv2.dnn.blobFromImage(image, 1 / 255.0, (416, 416),
                                 swapRB=True, crop=False)
    model.setInput(blob)
    layer_outputs = model.forward(ln)
    return get_num_person_predictions(layer_outputs)


app = Flask(__name__)
CORS(app)
model = get_machine_learning_model()


def get_image_from_pi_server():
    """
    Returns the image from the pi server in base64.
    :return: the image from the current available room.
    """
    address = 'http://192.168.3.2:6000/api/camera'
    res = requests.get(address).json()
    return res['image']


def base64_to_cv2_image(base64_str):
    """
    Convert a bas64 string into an openCV image.
    :param base64_str: the bas64 representation of the image.
    :return: the openCV image.
    """
    image_base64 = bytes(base64_str, encoding="ascii")
    image = Image.open(BytesIO(base64.b64decode(image_base64)))
    image = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
    return image


@app.route('/num_persons', methods=['GET'])
def get_num_persons():
    # retrieve the image from the pi server
    img = get_image_from_pi_server()

    # since the image is in base64 we need to convert it to an openCV image
    img = base64_to_cv2_image(img)

    room = 'TestRoom'
    # predict the number of persons from the image
    resp = json.dumps({'room': room, 'num_persons': get_number_of_persons(model, img)})

    # prepare the response...
    resp = Response(response=resp,
                    status=200,
                    mimetype="application/json")

    return resp


if __name__ == '__main__':
    app.run(port=6001)
