[#ftl output_format="HTML"]
 <!DOCTYPE html>
    <html lang="en">
      <head>
        <meta charset="UTF-8" />
        <meta http-equiv="X-UA-Compatible" content="IE=edge" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <style>
          * {
            box-sizing: border-box;
          }
    
          body {
            margin: 0;
          }
    
          * {
            margin-top: 0px;
            margin-right: 0px;
            margin-bottom: 0px;
            margin-left: 0px;
            padding-top: 0px;
            padding-right: 0px;
            padding-bottom: 0px;
            padding-left: 0px;
            font-family: 'Open Sans', sans-serif;
          }
    
          .input {
            transition-duration: 0.2s;
            transition-timing-function: ease-in-out;
            transition-delay: 0s;
            transition-property: border;
            min-width: 280px;
          }
    
          .input:focus + .label,
          .input:active + .label,
          .input.filled + .label {
            font-size: 0.75rem;
            transition-duration: 0.2s;
            transition-timing-function: ease-out;
            transition-delay: 0s;
            transition-property: all;
            top: -0.9rem;
            background-color: rgb(255, 255, 255);
            color: rgb(26, 115, 232);
            padding-top: 0px;
            padding-right: 5px;
            padding-bottom: 0px;
            padding-left: 5px;
            margin-top: 0px;
            margin-right: 5px;
            margin-bottom: 0px;
            margin-left: 5px;
          }
    
          .label {
            transition-duration: 0.2s;
            transition-timing-function: ease-out;
            transition-delay: 0s;
            transition-property: all;
            top: 0.4rem;
            left: 0px;
          }
    
          .w-full.rounded.px-3.border.border-gray-300.pt-5.pb-2.focus:border-green-700.focus:ring-1.focus:ring-green-700.focus:outline-none.input.active:outline-none {
            background-color: #eeeeee;
          }
    
          ::-webkit-datetime-edit-year-field:not([aria-valuenow]),
          ::-webkit-datetime-edit-month-field:not([aria-valuenow]),
          ::-webkit-datetime-edit-day-field:not([aria-valuenow]) {
            color: transparent;
          }
        </style>
        <title>Complete_Form</title>
        <link
          rel="stylesheet"
          href="https://cdn.jsdelivr.net/npm/tailwindcss@2.2/dist/tailwind.min.css"
          type="text/css"
        />
      </head>
    
      <body>
        <div class="md:h-screen bg-white relative flex flex-col justify-center items-center">
          <div
            class="md:border md:border-gray-300 bg-white md:shadow-lg shadow-none rounded p-10 align-middle"
          >
            <div class="flex flex-col items-center space-y-3">
              <div class="flex">
                <img src="https://gluu.org/wp-content/uploads/2020/12/logo.png" />
              </div>
              <span class="text-2xl font-semi-bold leading-normal">Form</span>
              <p class="leading-normal">Sample Form</p>
            </div>
            <form method="post" class="my-8">
              <div class="grid grid-cols-2 gap-2">
                <div>
                  <div class="relative mb-4">
                    <input
                      type="text"
                      id="displayName"
                      name="displayName"
                      autofocus
                      required
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    />
                    <label
                      for="displayName"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Display Name
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <input
                      type="email"
                      id="email"
                      name="email"
                      required
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    />
                    <label
                      for="email"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Email
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <input
                      type="text"
                      onfocus="(this.type='date')"
                      onblur="if(!this.value)this.type='text'"
                      id="birthday"
                      name="birthday"
                      required
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    />
                    <label
                      for="birthday"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Birthday
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <select
                      id="gender"
                      name="gender"
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    >
                      <option value=""></option>
                      <option value="male">Male</option>
                      <option value="female">Female</option>
                    </select>
                    <label
                      for="gender"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Gender
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <select
                      id="gender"
                      name="gender"
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    >
                      <option value=""></option>
                      <option value="active">Active</option>
                      <option value="inactive">Inactive</option>
                    </select>
                    <label
                      for="gender"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Status
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <input
                      type="file"
                      id="avatar"
                      name="avatar"
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    />
                  </div>
                </div>
                <div>
                  <div class="relative mb-4">
                    <textarea
                      id="description"
                      name="description"
                      rows="4"
                      required
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    ></textarea>
                    <label
                      for="description"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Description
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <input
                      type="text"
                      id="secret"
                      name="secret"
                      required
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    />
                    <label
                      for="secret"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Secret
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <input
                      type="password"
                      id="password"
                      required
                      name="password"
                      class="w-full rounded px-3 border border-gray-300 pt-5 pb-2 focus:border-green-700 focus:ring-1 focus:ring-green-700 focus:outline-none input active:outline-none"
                    />
                    <label
                      for="password"
                      class="label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base cursor-text"
                    >
                      Password
                    </label>
                  </div>
                  <div class="relative mb-4">
                    <label
                      for="check"
                      class="relative label absolute mt-2 ml-3 leading-tighter text-gray-600 text-base ml-4 cursor-pointer"
                      style="top: 0px; margin-right: 10px"
                    >
                      Checkbox
                    </label>
                    <input
                      type="checkbox"
                      id="check"
                      name="check"
                      class="rounded px-3 border border-gray-300 pt-5 pb-2 mr-3"
                    />
                  </div>
                </div>
              </div>
              <div class="space-y-9 mt-8">
                <div class="text-sm flex justify-center items-center">
                  <button
                    type="submit"
                    class="py-3 px-8 rounded text-white btn bg-green-500 hover:bg-green-600"
                  >
                    Submit
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      </body>
    </html>
    